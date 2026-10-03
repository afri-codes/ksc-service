-- Client dashboard: one client's sites, contracts, invoices, payments and upcoming jobs as one JSON document.
-- A client's sites are those they own (sites.site_owner_id); their contracts are those with client_id = the
-- client or on one of their sites. Soft-deleted rows (deleted_at set) are ignored.
-- Installed (CREATE OR REPLACE) at startup by DashboardFunctionInstaller.
CREATE OR REPLACE FUNCTION ksc_client_dashboard(p_client_id text)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $fn$
DECLARE
    v_today date := current_date;
BEGIN
    RETURN (
        WITH my_sites AS (
            SELECT s.* FROM sites s WHERE s.deleted_at IS NULL AND s.site_owner_id = p_client_id
        ),
        my_contracts AS (
            SELECT c.* FROM contracts c
            WHERE c.deleted_at IS NULL
              AND (c.client_id = p_client_id OR c.site_id IN (SELECT id FROM my_sites))
        ),
        my_invoices AS (
            SELECT i.* FROM invoices i
            WHERE i.deleted_at IS NULL AND COALESCE(i.status, '') <> 'CANCELLED'
              AND (i.site_id IN (SELECT id FROM my_sites) OR i.contract_id IN (SELECT id FROM my_contracts))
        ),
        my_payments AS (
            SELECT p.*, i.invoice_number FROM payments p JOIN invoices i ON i.id = p.invoice_id
            WHERE p.deleted_at IS NULL
              AND (i.site_id IN (SELECT id FROM my_sites) OR i.contract_id IN (SELECT id FROM my_contracts))
        )
        SELECT jsonb_build_object(
            'generatedAt', now(),
            'clientId', p_client_id,

            'sites', jsonb_build_object(
                'total', (SELECT count(*) FROM my_sites),
                'list', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object(
                               'siteId', s.id, 'addressArea', s.address_area, 'siteType', s.site_type,
                               'service', sv.service_name, 'totalPrice', s.total_price) ORDER BY s.created_at DESC)
                    FROM my_sites s LEFT JOIN services sv ON sv.id = s.service_id), '[]'::jsonb)
            ),

            'contracts', jsonb_build_object(
                'byStatus', COALESCE((SELECT jsonb_object_agg(COALESCE(status, 'UNKNOWN'), cnt)
                                      FROM (SELECT status, count(*) AS cnt FROM my_contracts GROUP BY status) x), '{}'::jsonb),
                'current', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object(
                               'contractId', c.id, 'contractNumber', c.contract_number, 'status', c.status,
                               'serviceType', c.service_type, 'startDate', c.start_date, 'endDate', c.end_date,
                               'contractValue', c.contract_value,
                               'expiringSoon', (c.status = 'ACTIVE' AND c.end_date BETWEEN v_today AND v_today + 30))
                               ORDER BY c.end_date NULLS LAST)
                    FROM my_contracts c
                    WHERE c.status IN ('DRAFT', 'SENT', 'SIGNED', 'ACTIVE')), '[]'::jsonb)
            ),

            'invoices', (
                SELECT jsonb_build_object(
                    'totalInvoiced', COALESCE(sum(amount_due), 0),
                    'totalPaid',     COALESCE(sum(COALESCE(amount_paid, 0)), 0),
                    'outstanding',   COALESCE(sum(GREATEST(amount_due - COALESCE(amount_paid, 0), 0))
                                              FILTER (WHERE status IN ('PENDING', 'PARTIALLY_PAID')), 0),
                    'overdueCount',  count(*) FILTER (WHERE status IN ('PENDING', 'PARTIALLY_PAID') AND due_date < v_today),
                    'overdueAmount', COALESCE(sum(GREATEST(amount_due - COALESCE(amount_paid, 0), 0))
                                              FILTER (WHERE status IN ('PENDING', 'PARTIALLY_PAID') AND due_date < v_today), 0),
                    'open', COALESCE((
                        SELECT jsonb_agg(jsonb_build_object(
                                   'invoiceId', i.id, 'invoiceNumber', i.invoice_number, 'status', i.status,
                                   'amountDue', i.amount_due, 'amountPaid', COALESCE(i.amount_paid, 0),
                                   'balanceDue', GREATEST(i.amount_due - COALESCE(i.amount_paid, 0), 0),
                                   'dueDate', i.due_date, 'overdue', (i.due_date < v_today)) ORDER BY i.due_date NULLS LAST)
                        FROM my_invoices i
                        WHERE i.status IN ('PENDING', 'PARTIALLY_PAID')), '[]'::jsonb))
                FROM my_invoices
            ),

            'payments', jsonb_build_object(
                'totalPaid', (SELECT COALESCE(sum(amount), 0) FROM my_payments WHERE status = 'SUCCESS'),
                'inProgress', (SELECT count(*) FROM my_payments WHERE status IN ('CREATED', 'PENDING', 'PROCESSING')),
                'recent', COALESCE((
                    SELECT jsonb_agg(jsonb_build_object(
                               'paymentId', x.id, 'receiptNumber', x.receipt_number, 'invoiceNumber', x.invoice_number,
                               'amount', x.amount, 'method', x.method, 'status', x.status,
                               'paidAt', x.paid_at, 'requestedAt', x.created_at) ORDER BY x.created_at DESC)
                    FROM (SELECT * FROM my_payments ORDER BY created_at DESC NULLS LAST LIMIT 10) x), '[]'::jsonb)
            ),

            'upcomingJobs', COALESCE((
                SELECT jsonb_agg(jsonb_build_object(
                           'jobId', x.id, 'status', x.status, 'serviceType', x.service_type,
                           'scheduledStart', x.scheduled_start, 'scheduledEnd', x.scheduled_end,
                           'siteId', x.site_id, 'addressArea', x.address_area) ORDER BY x.scheduled_start)
                FROM (SELECT j.id, j.status, j.service_type, j.scheduled_start, j.scheduled_end, j.site_id, s.address_area
                      FROM jobs j JOIN my_sites s ON s.id = j.site_id
                      WHERE j.deleted_at IS NULL AND j.scheduled_start >= now()
                        AND COALESCE(j.status, '') NOT IN ('COMPLETED', 'CANCELLED', 'FAILED')
                      ORDER BY j.scheduled_start
                      LIMIT 5) x), '[]'::jsonb)
        )
    );
END;
$fn$;
