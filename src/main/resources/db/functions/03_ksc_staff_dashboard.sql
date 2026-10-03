-- Staff dashboard: one staff member's crews, jobs, clock-in state, attendance (last 30 days) and ratings.
-- p_staff_id is the staff member's user ID (crew_members.staff_id, time_logs.staff_id, staff_ratings.staff_id).
-- Jobs are assigned to crews, so "my jobs" are jobs of crews the staff member belongs to (crew_members).
-- Soft-deleted rows (deleted_at set) are ignored. Installed (CREATE OR REPLACE) at startup by DashboardFunctionInstaller.
CREATE OR REPLACE FUNCTION ksc_staff_dashboard(p_staff_id text)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $fn$
DECLARE
    v_today  timestamp := date_trunc('day', now());
    v_since  timestamp := date_trunc('day', now()) - interval '29 days';
BEGIN
    RETURN (
        WITH my_crews AS (
            SELECT c.id, c.crew_name, c.zone_name, c.supervisor_name, m.member_role
            FROM crew_members m JOIN crews c ON c.id = m.crew_id
            WHERE m.deleted_at IS NULL AND c.deleted_at IS NULL AND m.staff_id = p_staff_id
        ),
        my_jobs AS (
            SELECT j.*, s.address_area, mc.crew_name
            FROM jobs j
            JOIN my_crews mc ON mc.id = j.crew_id
            LEFT JOIN sites s ON s.id = j.site_id
            WHERE j.deleted_at IS NULL
        ),
        my_logs AS (
            SELECT t.* FROM time_logs t WHERE t.deleted_at IS NULL AND t.staff_id::text = p_staff_id
        )
        SELECT jsonb_build_object(
            'generatedAt', now(),
            'staffId', p_staff_id,

            'crews', COALESCE((SELECT jsonb_agg(jsonb_build_object(
                                   'crewId', id, 'crewName', crew_name, 'zone', zone_name,
                                   'supervisorName', supervisor_name, 'role', member_role) ORDER BY crew_name)
                               FROM my_crews), '[]'::jsonb),

            'today', COALESCE((
                SELECT jsonb_agg(jsonb_build_object(
                           'jobId', id, 'crewName', crew_name, 'status', status, 'serviceType', service_type,
                           'scheduledStart', scheduled_start, 'scheduledEnd', scheduled_end, 'siteId', site_id,
                           'addressArea', address_area) ORDER BY scheduled_start)
                FROM my_jobs
                WHERE scheduled_start >= v_today AND scheduled_start < v_today + interval '1 day'), '[]'::jsonb),

            'upcomingJobs', COALESCE((
                SELECT jsonb_agg(jsonb_build_object(
                           'jobId', x.id, 'crewName', x.crew_name, 'status', x.status, 'serviceType', x.service_type,
                           'scheduledStart', x.scheduled_start, 'scheduledEnd', x.scheduled_end, 'siteId', x.site_id,
                           'addressArea', x.address_area) ORDER BY x.scheduled_start)
                FROM (SELECT * FROM my_jobs
                      WHERE scheduled_start >= v_today + interval '1 day'
                        AND COALESCE(status, '') NOT IN ('COMPLETED', 'CANCELLED', 'FAILED')
                      ORDER BY scheduled_start LIMIT 10) x), '[]'::jsonb),

            'clockedIn', (
                SELECT jsonb_build_object('timeLogId', t.id, 'jobId', t.job_id, 'clockIn', t.clock_in,
                                          'clockInStatus', t.clock_in_status, 'addressArea', s.address_area)
                FROM my_logs t
                LEFT JOIN jobs j ON j.id = t.job_id
                LEFT JOIN sites s ON s.id = j.site_id
                WHERE t.clock_out IS NULL
                ORDER BY t.clock_in DESC
                LIMIT 1),

            'attendanceLast30Days', (
                SELECT jsonb_build_object(
                    'daysWorked', count(DISTINCT date_trunc('day', clock_in)),
                    'clockIns', count(*),
                    'jobsWorked', count(DISTINCT job_id),
                    'hoursWorked', COALESCE(round((sum(EXTRACT(EPOCH FROM (clock_out - clock_in))) FILTER (WHERE clock_out IS NOT NULL) / 3600)::numeric, 2), 0),
                    'outOfSiteEvents', count(*) FILTER (WHERE clock_in_status = 'OUT_OF_SITE' OR clock_out_status = 'OUT_OF_SITE'),
                    'missingClockOuts', count(*) FILTER (WHERE clock_out IS NULL AND clock_in < v_today))
                FROM my_logs WHERE clock_in >= v_since),

            'jobsByStatusLast30Days', COALESCE((
                SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                FROM (SELECT status, count(*) AS cnt FROM my_jobs
                      WHERE scheduled_start >= v_since AND scheduled_start < v_today + interval '1 day'
                      GROUP BY status) x), '{}'::jsonb),

            'ratings', (
                SELECT jsonb_build_object(
                    'average', round(avg(r.rating)::numeric, 2),
                    'count', count(*),
                    'recent', COALESCE((
                        SELECT jsonb_agg(jsonb_build_object('rating', y.rating, 'comment', y.comment, 'ratedAt', y.created_at)
                                         ORDER BY y.created_at DESC)
                        FROM (SELECT rating, comment, created_at FROM staff_ratings
                              WHERE deleted_at IS NULL AND staff_id = p_staff_id
                              ORDER BY created_at DESC LIMIT 5) y), '[]'::jsonb))
                FROM staff_ratings r
                WHERE r.deleted_at IS NULL AND r.staff_id = p_staff_id)
        )
    );
END;
$fn$;
