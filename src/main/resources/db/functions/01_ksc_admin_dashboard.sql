-- Super admin dashboard: company-wide figures as one JSON document.
-- p_from / p_to (inclusive) bound the "in period" figures (new sites, new leads, payments received);
-- everything else is the current position. Soft-deleted rows (deleted_at set) are ignored.
-- Installed (CREATE OR REPLACE) at startup by DashboardFunctionInstaller.
CREATE OR REPLACE FUNCTION ksc_admin_dashboard(p_from date, p_to date)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $fn$
DECLARE
    v_from timestamp := p_from::timestamp;
    v_to   timestamp := (p_to + 1)::timestamp;   -- exclusive upper bound
    v_today date := current_date;
BEGIN
    RETURN jsonb_build_object(
        'generatedAt', now(),
        'period', jsonb_build_object('from', p_from, 'to', p_to),

        'sites', jsonb_build_object(
            'total',       (SELECT count(*) FROM sites WHERE deleted_at IS NULL),
            'newInPeriod', (SELECT count(*) FROM sites WHERE deleted_at IS NULL AND created_at >= v_from AND created_at < v_to),
            'byService', COALESCE((
                SELECT jsonb_agg(jsonb_build_object('service', x.service, 'count', x.cnt, 'totalValue', x.total) ORDER BY x.cnt DESC)
                FROM (SELECT COALESCE(sv.service_name, 'Unassigned') AS service, count(*) AS cnt, COALESCE(sum(s.total_price), 0) AS total
                      FROM sites s LEFT JOIN services sv ON sv.id = s.service_id
                      WHERE s.deleted_at IS NULL
                      GROUP BY 1) x), '[]'::jsonb)
        ),

        'leads', jsonb_build_object(
            'total',       (SELECT count(*) FROM leads WHERE deleted_at IS NULL),
            'newInPeriod', (SELECT count(*) FROM leads WHERE deleted_at IS NULL AND created_at >= v_from AND created_at < v_to),
            'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                  FROM (SELECT status, count(*) AS cnt FROM leads WHERE deleted_at IS NULL GROUP BY status) x), '{}'::jsonb)
        ),

        'quotes', jsonb_build_object(
            'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                  FROM (SELECT status, count(*) AS cnt FROM quotes WHERE deleted_at IS NULL GROUP BY status) x), '{}'::jsonb)
        ),

        'contracts', jsonb_build_object(
            'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                  FROM (SELECT status, count(*) AS cnt FROM contracts WHERE deleted_at IS NULL GROUP BY status) x), '{}'::jsonb),
            'activeCount', (SELECT count(*) FROM contracts WHERE deleted_at IS NULL AND status = 'ACTIVE'),
            'activeValue', (SELECT COALESCE(sum(contract_value), 0) FROM contracts WHERE deleted_at IS NULL AND status = 'ACTIVE'),
            'expiringIn30Days', (SELECT count(*) FROM contracts
                                 WHERE deleted_at IS NULL AND status = 'ACTIVE' AND end_date BETWEEN v_today AND v_today + 30)
        ),

        'subscriptions', jsonb_build_object(
            'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                  FROM (SELECT status, count(*) AS cnt FROM subscriptions WHERE deleted_at IS NULL GROUP BY status) x), '{}'::jsonb)
        ),

        'jobs', jsonb_build_object(
            'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                  FROM (SELECT status, count(*) AS cnt FROM jobs WHERE deleted_at IS NULL GROUP BY status) x), '{}'::jsonb),
            'upcoming', (SELECT count(*) FROM jobs WHERE deleted_at IS NULL AND scheduled_start >= now()
                         AND COALESCE(status, '') NOT IN ('COMPLETED', 'CANCELLED', 'FAILED'))
        ),

        'invoices', (
            SELECT jsonb_build_object(
                'count',          count(*),
                'totalInvoiced',  COALESCE(sum(amount_due), 0),
                'totalPaid',      COALESCE(sum(COALESCE(amount_paid, 0)), 0),
                'outstanding',    COALESCE(sum(GREATEST(amount_due - COALESCE(amount_paid, 0), 0))
                                           FILTER (WHERE status IN ('PENDING', 'PARTIALLY_PAID')), 0),
                'overdueCount',   count(*) FILTER (WHERE status IN ('PENDING', 'PARTIALLY_PAID') AND due_date < v_today),
                'overdueAmount',  COALESCE(sum(GREATEST(amount_due - COALESCE(amount_paid, 0), 0))
                                           FILTER (WHERE status IN ('PENDING', 'PARTIALLY_PAID') AND due_date < v_today), 0),
                'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                      FROM (SELECT status, count(*) AS cnt FROM invoices WHERE deleted_at IS NULL GROUP BY status) x), '{}'::jsonb))
            FROM invoices
            WHERE deleted_at IS NULL AND COALESCE(status, '') <> 'CANCELLED'
        ),

        'payments', jsonb_build_object(
            'receivedInPeriod', (SELECT COALESCE(sum(amount), 0) FROM payments
                                 WHERE deleted_at IS NULL AND status = 'SUCCESS' AND paid_at >= v_from AND paid_at < v_to),
            'countInPeriod',    (SELECT count(*) FROM payments
                                 WHERE deleted_at IS NULL AND status = 'SUCCESS' AND paid_at >= v_from AND paid_at < v_to),
            'failedInPeriod',   (SELECT count(*) FROM payments
                                 WHERE deleted_at IS NULL AND status IN ('FAILED', 'CANCELLED') AND created_at >= v_from AND created_at < v_to),
            'inProgress',       (SELECT count(*) FROM payments
                                 WHERE deleted_at IS NULL AND status IN ('CREATED', 'PENDING', 'PROCESSING')),
            'byMethod', COALESCE((
                SELECT jsonb_agg(jsonb_build_object('method', x.method, 'count', x.cnt, 'amount', x.total) ORDER BY x.total DESC)
                FROM (SELECT COALESCE(method, 'OTHER') AS method, count(*) AS cnt, sum(amount) AS total
                      FROM payments
                      WHERE deleted_at IS NULL AND status = 'SUCCESS' AND paid_at >= v_from AND paid_at < v_to
                      GROUP BY 1) x), '[]'::jsonb)
        ),

        -- Last 12 calendar months including the current one, with zero months filled in.
        'revenueByMonth', (
            SELECT jsonb_agg(jsonb_build_object('month', to_char(m.month, 'YYYY-MM'), 'amount', COALESCE(r.total, 0)) ORDER BY m.month)
            FROM generate_series(date_trunc('month', now()) - interval '11 months', date_trunc('month', now()), interval '1 month') AS m(month)
            LEFT JOIN (SELECT date_trunc('month', paid_at) AS month, sum(amount) AS total
                       FROM payments
                       WHERE deleted_at IS NULL AND status = 'SUCCESS'
                         AND paid_at >= date_trunc('month', now()) - interval '11 months'
                       GROUP BY 1) r ON r.month = m.month
        ),

        'recentPayments', COALESCE((
            SELECT jsonb_agg(jsonb_build_object(
                       'paymentId', x.id, 'receiptNumber', x.receipt_number, 'invoiceNumber', x.invoice_number,
                       'amount', x.amount, 'method', x.method, 'paidAt', x.paid_at) ORDER BY x.paid_at DESC)
            FROM (SELECT p.id, p.receipt_number, i.invoice_number, p.amount, p.method, p.paid_at
                  FROM payments p JOIN invoices i ON i.id = p.invoice_id
                  WHERE p.deleted_at IS NULL AND p.status = 'SUCCESS'
                  ORDER BY p.paid_at DESC NULLS LAST
                  LIMIT 10) x), '[]'::jsonb)
    );
END;
$fn$;
