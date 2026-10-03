package ksc.go.tz.common.pdf;

import ksc.go.tz.common.LineItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Everything needed to render a quotation or invoice PDF.
 *
 * @param title          document title, e.g. "QUOTATION" or "INVOICE"
 * @param number         document number, e.g. "QT-20261002-7K3Q9A"
 * @param issueDate      date the document was issued
 * @param secondaryLabel label for the second date, e.g. "Valid until" or "Due date"
 * @param secondaryDate  value for the second date
 * @param status         document status, e.g. "DRAFT" or "PENDING"
 * @param billTo         lines describing the client / site being billed
 * @param items          priced line items
 * @param total          total amount in TZS
 * @param notes          closing notes printed under the total
 */
public record BillingDocument(
        String title,
        String number,
        LocalDate issueDate,
        String secondaryLabel,
        LocalDate secondaryDate,
        String status,
        List<String> billTo,
        List<LineItem> items,
        BigDecimal total,
        List<String> notes
) {
}
