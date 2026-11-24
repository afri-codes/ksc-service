-- Supervisor dashboard: the crews a supervisor leads (crews.supervisor_id) and their members, jobs, attendance,
-- staff performance, client feedback and checklist completion. p_from / p_to (inclusive) bound the period figures.
-- Soft-deleted rows (deleted_at set) are ignored. Installed (CREATE OR REPLACE) at startup by DashboardFunctionInstaller.
CREATE OR REPLACE FUNCTION ksc_supervisor_dashboard(p_supervisor_id text, p_from date, p_to date)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $fn$
DECLARE
    v_from  timestamp := p_from::timestamp;
    v_to    timestamp := (p_to + 1)::timestamp;   -- exclusive upper bound
    v_today timestamp := date_trunc('day', now());
BEGIN
    RETURN (
        WITH my_crews AS (
            SELECT c.* FROM crews c WHERE c.deleted_at IS NULL AND c.supervisor_id = p_supervisor_id
        ),
        my_members AS (
            SELECT m.* FROM crew_members m WHERE m.deleted_at IS NULL AND m.crew_id IN (SELECT id FROM my_crews)
        ),
        my_jobs AS (
            SELECT j.*, s.address_area, c.crew_name
            FROM jobs j JOIN my_crews c ON c.id = j.crew_id LEFT JOIN sites s ON s.id = j.site_id
            WHERE j.deleted_at IS NULL
        ),
        period_jobs AS (
            SELECT * FROM my_jobs WHERE scheduled_start >= v_from AND scheduled_start < v_to
        ),
        my_logs AS (
            SELECT t.* FROM time_logs t WHERE t.deleted_at IS NULL AND t.job_id IN (SELECT id FROM my_jobs)
        ),
        my_feedback AS (
            SELECT f.* FROM feedbacks f WHERE f.deleted_at IS NULL AND f.job_id IN (SELECT id FROM my_jobs)
        )
        SELECT jsonb_build_object(
            'generatedAt', now(),
            'supervisorId', p_supervisor_id,
            'period', jsonb_build_object('from', p_from, 'to', p_to),

            'crews', COALESCE((
                SELECT jsonb_agg(jsonb_build_object(
                           'crewId', c.id, 'crewName', c.crew_name, 'zone', c.zone_name,
                           'members', COALESCE((SELECT jsonb_agg(jsonb_build_object('staffId', m.staff_id, 'staffName', m.staff_name,
                                                                                    'role', m.member_role) ORDER BY m.staff_name)
                                                FROM my_members m WHERE m.crew_id = c.id), '[]'::jsonb))
                       ORDER BY c.crew_name)
                FROM my_crews c), '[]'::jsonb),

            'jobs', jsonb_build_object(
                'byStatusInPeriod', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                              FROM (SELECT status, count(*) AS cnt FROM period_jobs GROUP BY status) x), '{}'::jsonb),
                'todayCount', (SELECT count(*) FROM my_jobs WHERE scheduled_start >= v_today AND scheduled_start < v_today + interval '1 day'),
                'today', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object('jobId', id, 'crewName', crew_name, 'status', status,
                                                        'serviceType', service_type, 'scheduledStart', scheduled_start,
                                                        'scheduledEnd', scheduled_end, 'addressArea', address_area)
                                     ORDER BY scheduled_start)
                    FROM my_jobs WHERE scheduled_start >= v_today AND scheduled_start < v_today + interval '1 day'), '[]'::jsonb),
                -- Should have finished but are still open.
                'late', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object('jobId', id, 'crewName', crew_name, 'status', status,
                                                        'scheduledEnd', scheduled_end, 'addressArea', address_area)
                                     ORDER BY scheduled_end)
                    FROM my_jobs
                    WHERE scheduled_end < now() AND COALESCE(status, '') NOT IN ('COMPLETED', 'CANCELLED', 'FAILED')), '[]'::jsonb),
                'upcoming', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object('jobId', x.id, 'crewName', x.crew_name, 'status', x.status,
                                                        'serviceType', x.service_type, 'scheduledStart', x.scheduled_start,
                                                        'addressArea', x.address_area) ORDER BY x.scheduled_start)
                    FROM (SELECT * FROM my_jobs
                          WHERE scheduled_start >= v_today + interval '1 day'
                            AND COALESCE(status, '') NOT IN ('COMPLETED', 'CANCELLED', 'FAILED')
                          ORDER BY scheduled_start LIMIT 10) x), '[]'::jsonb)
            ),

            'attendance', jsonb_build_object(
                'onSiteNow', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object('staffId', t.staff_id, 'staffName', mm.staff_name, 'jobId', t.job_id,
                                                        'addressArea', j.address_area, 'since', t.clock_in,
                                                        'clockInStatus', t.clock_in_status) ORDER BY t.clock_in)
                    FROM my_logs t
                    JOIN my_jobs j ON j.id = t.job_id
                    LEFT JOIN LATERAL (SELECT staff_name FROM my_members m WHERE m.staff_id = t.staff_id::text LIMIT 1) mm ON true
                    WHERE t.clock_out IS NULL AND t.clock_in >= v_today - interval '1 day'), '[]'::jsonb),
                'clockInsInPeriod', (SELECT count(*) FROM my_logs WHERE clock_in >= v_from AND clock_in < v_to),
                'hoursInPeriod', (SELECT COALESCE(round((sum(EXTRACT(EPOCH FROM (clock_out - clock_in))) / 3600)::numeric, 2), 0)
                                  FROM my_logs WHERE clock_in >= v_from AND clock_in < v_to AND clock_out IS NOT NULL),
                'outOfSiteEventsInPeriod', (SELECT count(*) FROM my_logs
                                            WHERE clock_in >= v_from AND clock_in < v_to
                                              AND (clock_in_status = 'OUT_OF_SITE' OR clock_out_status = 'OUT_OF_SITE')),
                'missingClockOuts', (SELECT count(*) FROM my_logs WHERE clock_out IS NULL AND clock_in < v_today)
            ),

            -- One row per crew member, plus anyone else who clocked in on the supervisor's jobs in the period.
            'staffPerformance', COALESCE((
                SELECT jsonb_agg(jsonb_build_object(
                           'staffId', p.staff_id, 'staffName', p.staff_name,
                           'jobsWorked', p.jobs_worked, 'hoursWorked', p.hours_worked,
                           'averageRating', p.avg_rating, 'ratingsCount', p.ratings_count)
                       ORDER BY p.staff_name NULLS LAST, p.staff_id)
                FROM (
                    SELECT st.staff_id,
                           (SELECT staff_name FROM my_members m WHERE m.staff_id = st.staff_id LIMIT 1) AS staff_name,
                           (SELECT count(DISTINCT t.job_id) FROM my_logs t
                             WHERE t.staff_id::text = st.staff_id AND t.clock_in >= v_from AND t.clock_in < v_to) AS jobs_worked,
                           (SELECT COALESCE(round((sum(EXTRACT(EPOCH FROM (t.clock_out - t.clock_in))) / 3600)::numeric, 2), 0)
                              FROM my_logs t
                             WHERE t.staff_id::text = st.staff_id AND t.clock_in >= v_from AND t.clock_in < v_to
                               AND t.clock_out IS NOT NULL) AS hours_worked,
                           (SELECT round(avg(r.rating)::numeric, 2) FROM staff_ratings r JOIN my_feedback f ON f.id = r.feedback_id
                             WHERE r.deleted_at IS NULL AND r.staff_id = st.staff_id
                               AND r.created_at >= v_from AND r.created_at < v_to) AS avg_rating,
                           (SELECT count(*) FROM staff_ratings r JOIN my_feedback f ON f.id = r.feedback_id
                             WHERE r.deleted_at IS NULL AND r.staff_id = st.staff_id
                               AND r.created_at >= v_from AND r.created_at < v_to) AS ratings_count
                    FROM (SELECT staff_id FROM my_members
                          UNION
                          SELECT staff_id::text FROM my_logs WHERE clock_in >= v_from AND clock_in < v_to) st
                ) p), '[]'::jsonb),

            'feedback', jsonb_build_object(
                'countInPeriod', (SELECT count(*) FROM my_feedback WHERE created_at >= v_from AND created_at < v_to),
                'averageServiceRating', (SELECT round(avg(service_rating)::numeric, 2) FROM my_feedback
                                         WHERE created_at >= v_from AND created_at < v_to),
                'openDisputes', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object('feedbackId', f.id, 'jobId', f.job_id, 'serviceRating', f.service_rating,
                                                        'comment', f.comment, 'disputeStatus', f.dispute_status,
                                                        'createdAt', f.created_at) ORDER BY f.created_at)
                    FROM my_feedback f WHERE f.dispute_status IN ('OPEN', 'UNDER_REVIEW')), '[]'::jsonb)
            ),

            'checklists', (
                SELECT jsonb_build_object(
                    'itemsInPeriod', count(*),
                    'completedItems', count(*) FILTER (WHERE COALESCE(i.is_complete, false)),
                    'completionRate', CASE WHEN count(*) = 0 THEN NULL
                                           ELSE round(100.0 * count(*) FILTER (WHERE COALESCE(i.is_complete, false)) / count(*), 1) END)
                FROM checklist_items i
                JOIN job_checklists cl ON cl.id = i.checklist_id AND cl.deleted_at IS NULL
                JOIN period_jobs pj ON pj.id = cl.job_id
                WHERE i.deleted_at IS NULL)
        )
    );
END;
$fn$;
