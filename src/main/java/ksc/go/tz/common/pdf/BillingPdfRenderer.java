package ksc.go.tz.common.pdf;

import afriUtils.responses.AfriException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Renders quotations and invoices as A4 PDFs using Apache PDFBox.
 */
@Component
@RequiredArgsConstructor
public class BillingPdfRenderer {

    private final CompanyDetails company;

    public byte[] render(BillingDocument doc) {
        try (PDDocument pdf = new PDDocument()) {
            try (PdfCanvas canvas = new PdfCanvas(pdf)) {
                canvas.header(company, doc.title(), doc.number(), List.of(
                        "Date: " + format(doc.issueDate()),
                        doc.secondaryLabel() + ": " + format(doc.secondaryDate()),
                        "Status: " + doc.status()));
                canvas.labelledBlock("BILL TO", doc.billTo(), PdfCanvas.MARGIN, PdfCanvas.CONTENT_WIDTH);
                canvas.y -= 14;
                canvas.itemsTable(doc.items());
                canvas.total("TOTAL", doc.total());
                canvas.notes(doc.notes());
                canvas.footer(company);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AfriException("Could not generate " + doc.title().toLowerCase(Locale.ROOT) + " PDF: " + e.getMessage());
        }
    }

    public static String format(LocalDate date) {
        return PdfCanvas.format(date);
    }
}
