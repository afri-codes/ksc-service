package ksc.go.tz.billing.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.billing.entities.Payment;
import ksc.go.tz.common.pdf.BillTo;
import ksc.go.tz.common.pdf.CompanyDetails;
import ksc.go.tz.common.pdf.PdfCanvas;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders the receipt for a successful payment as an A4 PDF.
 */
@Component
@RequiredArgsConstructor
public class ReceiptPdfRenderer {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH);

    private final CompanyDetails company;

    public byte[] render(Payment payment) {
        Invoice invoice = payment.getInvoice();
        try (PDDocument pdf = new PDDocument()) {
            try (PdfCanvas c = new PdfCanvas(pdf)) {
                c.header(company, "PAYMENT RECEIPT", payment.getReceiptNumber(), List.of(
                        "Date: " + PdfCanvas.format(payment.getPaidAt() != null ? payment.getPaidAt().toLocalDate() : null),
                        "Status: PAID"));

                c.labelledBlock("RECEIVED FROM", receivedFrom(payment, invoice), PdfCanvas.MARGIN, PdfCanvas.CONTENT_WIDTH);
                c.y -= 10;

                // Amount received, prominently.
                c.ensureSpace(50);
                c.fill(PdfCanvas.MARGIN, c.y - 34, PdfCanvas.CONTENT_WIDTH, 34, PdfCanvas.HEADER_BG);
                c.text("AMOUNT RECEIVED", PdfCanvas.BOLD, 9, PdfCanvas.MUTED, PdfCanvas.MARGIN + 10, c.y - 21);
                c.textRight(currency(payment) + " " + PdfCanvas.money(payment.getAmount()), PdfCanvas.BOLD, 16,
                        PdfCanvas.ACCENT, PdfCanvas.RIGHT - 10, c.y - 23);
                c.y -= 52;

                c.sectionTitle("Payment details");
                List<String[]> details = new ArrayList<>();
                details.add(new String[]{"Paid on", payment.getPaidAt() != null ? payment.getPaidAt().format(DATE_TIME) : "-"});
                details.add(new String[]{"Method", payment.getMethod() != null ? payment.getMethod().getDisplayName() : "-"});
                if (payment.getProviderRef() != null) {
                    details.add(new String[]{payment.getMethod() != null && "CASH".equals(payment.getMethod().name())
                            ? "Cash receipt no." : "Transaction reference", payment.getProviderRef()});
                }
                if (payment.getPaymentReference() != null) {
                    details.add(new String[]{"Payment reference", payment.getPaymentReference()});
                }
                if (payment.getPayerPhone() != null) {
                    details.add(new String[]{"Payer phone", payment.getPayerPhone()});
                }
                if (payment.getNotes() != null) {
                    details.add(new String[]{"Note", payment.getNotes()});
                }
                c.keyValueRows(details);
                c.y -= 8;

                c.sectionTitle("Invoice " + invoice.getInvoiceNumber());
                BigDecimal due = invoice.getAmountDue();
                BigDecimal paid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
                BigDecimal balance = due != null ? due.subtract(paid).max(BigDecimal.ZERO) : null;
                c.keyValueRows(List.of(
                        new String[]{"Invoice total", "TZS " + PdfCanvas.money(due)},
                        new String[]{"Paid to date", "TZS " + PdfCanvas.money(paid) + "  (as of " + PdfCanvas.format(LocalDate.now()) + ")"},
                        new String[]{"Balance remaining", "TZS " + PdfCanvas.money(balance)},
                        new String[]{"Invoice status", readable(invoice.getStatus())}
                ));
                c.y -= 10;

                c.notes(List.of(
                        "Thank you for your payment.",
                        "Keep this receipt as proof of payment. Quote receipt number " + payment.getReceiptNumber() + " in any enquiry."));
                c.footer(company);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AfriException("Could not generate payment receipt: " + e.getMessage());
        }
    }

    private static List<String> receivedFrom(Payment payment, Invoice invoice) {
        if (invoice.getSite() != null) {
            return BillTo.forSite(invoice.getSite());
        }
        return List.of(payment.getPayerPhone() != null ? payment.getPayerPhone() : "-");
    }

    /** PARTIALLY_PAID → "Partially paid". */
    private static String readable(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        String lower = status.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String currency(Payment payment) {
        return payment.getCurrency() != null ? payment.getCurrency() : "TZS";
    }
}
