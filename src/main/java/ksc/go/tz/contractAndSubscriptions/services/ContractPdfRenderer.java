package ksc.go.tz.contractAndSubscriptions.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.common.pdf.BillTo;
import ksc.go.tz.common.pdf.CompanyDetails;
import ksc.go.tz.common.pdf.PdfCanvas;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.quotation.entities.Quote;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders a service contract as an A4 PDF: parties, contract details, scope (the quote's line items),
 * terms and signature blocks.
 */
@Component
@RequiredArgsConstructor
public class ContractPdfRenderer {

    /** Printed when a contract has no terms of its own. Placeholder wording; replace with approved terms. */
    static final List<String> DEFAULT_TERMS = List.of(
            "1. The service provider will deliver the services described in the scope above at the site, at the stated frequency, for the contract period.",
            "2. The client will give the service provider's staff reasonable access to the site at agreed times.",
            "3. Payment is due as stated on each invoice issued under this contract.",
            "4. Either party may terminate this contract by giving written notice to the other party.",
            "5. Changes to the scope or price must be agreed in writing by both parties."
    );

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH);

    private final CompanyDetails company;

    public byte[] render(Contract contract) {
        String number = contract.getContractNumber() != null ? contract.getContractNumber() : "CT-" + contract.getId();
        try (PDDocument pdf = new PDDocument()) {
            try (PdfCanvas c = new PdfCanvas(pdf)) {
                c.header(company, "SERVICE CONTRACT", number, List.of(
                        "Date: " + PdfCanvas.format(contract.getCreatedAt() != null ? contract.getCreatedAt().toLocalDate() : null),
                        "Status: " + contract.getStatus()));

                // Parties, side by side.
                float half = (PdfCanvas.CONTENT_WIDTH - 20) / 2;
                float top = c.y;
                c.labelledBlock("SERVICE PROVIDER", List.of(company.getName(), company.getAddress(), company.contactLine()),
                        PdfCanvas.MARGIN, half);
                float leftBottom = c.y;
                c.y = top;
                c.labelledBlock("CLIENT", clientLines(contract), PdfCanvas.MARGIN + half + 20, half);
                c.y = Math.min(leftBottom, c.y) - 10;

                c.sectionTitle("Contract details");
                Quote quote = contract.getQuote();
                c.keyValueRows(List.of(
                        new String[]{"Service", contract.getServiceType() != null ? contract.getServiceType().getDisplayName() : "-"},
                        new String[]{"Period", PdfCanvas.format(contract.getStartDate()) + " to " + PdfCanvas.format(contract.getEndDate())},
                        new String[]{"Frequency", contract.getFrequency() != null ? title(contract.getFrequency().name()) : "-"},
                        new String[]{"Contract value", "TZS " + PdfCanvas.money(contract.getContractValue())},
                        new String[]{"Quotation", quote != null && quote.getQuoteNumber() != null ? quote.getQuoteNumber() : "-"}
                ));
                c.y -= 8;

                List<LineItem> items = quote != null ? quote.getItems() : List.of();
                if (!items.isEmpty()) {
                    c.sectionTitle("Scope and pricing (per quotation)");
                    c.itemsTable(items);
                    c.total("QUOTED TOTAL", quote.getPriceMax());
                }

                c.sectionTitle("Terms");
                List<String> terms = contract.getTerms() != null && !contract.getTerms().isBlank()
                        ? List.of(contract.getTerms().split("\\r?\\n"))
                        : DEFAULT_TERMS;
                for (String term : terms) {
                    for (String line : PdfCanvas.wrap(term, PdfCanvas.REGULAR, PdfCanvas.BODY_SIZE, PdfCanvas.CONTENT_WIDTH)) {
                        c.ensureSpace(PdfCanvas.ROW_LEADING);
                        c.text(line, PdfCanvas.REGULAR, PdfCanvas.BODY_SIZE, PdfCanvas.INK, PdfCanvas.MARGIN, c.y);
                        c.y -= PdfCanvas.ROW_LEADING;
                    }
                    c.y -= 3;
                }

                signatures(c, contract, half);
                c.footer(company);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AfriException("Could not generate contract PDF: " + e.getMessage());
        }
    }

    private List<String> clientLines(Contract contract) {
        List<String> lines = new ArrayList<>();
        if (contract.getBusinessInfo() != null) {
            lines.add(contract.getBusinessInfo());
        }
        if (contract.getBusinessTin() != null) {
            lines.add("TIN: " + contract.getBusinessTin());
        }
        if (contract.getBusinessBrelaNo() != null) {
            lines.add("BRELA No: " + contract.getBusinessBrelaNo());
        }
        if (contract.getPersonalIdNo() != null) {
            lines.add("ID No: " + contract.getPersonalIdNo());
        }
        if (contract.getSite() != null) {
            lines.addAll(BillTo.forSite(contract.getSite()));
        }
        lines.add("Client ref: " + contract.getClientId());
        return lines;
    }

    private void signatures(PdfCanvas c, Contract contract, float half) throws IOException {
        c.ensureSpace(110);
        c.y -= 18;
        float top = c.y;
        float rightX = PdfCanvas.MARGIN + half + 20;

        c.text("For " + company.getName(), PdfCanvas.BOLD, 9, PdfCanvas.MUTED, PdfCanvas.MARGIN, top);
        c.text("For the client", PdfCanvas.BOLD, 9, PdfCanvas.MUTED, rightX, top);

        float lineY = top - 40;
        c.rule(PdfCanvas.MARGIN, PdfCanvas.MARGIN + half, lineY, PdfCanvas.INK, 0.75f);
        c.rule(rightX, rightX + half, lineY, PdfCanvas.INK, 0.75f);
        c.text("Signature, name and date", PdfCanvas.REGULAR, 8, PdfCanvas.MUTED, PdfCanvas.MARGIN, lineY - 11);

        String clientCaption = contract.getSignedBy() != null
                ? "Signed by " + contract.getSignedBy()
                        + (contract.getSignedAt() != null ? " on " + contract.getSignedAt().format(DATE_TIME) : "")
                : "Signature, name and date";
        c.text(clientCaption, PdfCanvas.REGULAR, 8, PdfCanvas.MUTED, rightX, lineY - 11);
        c.y = lineY - 30;
    }

    private static String title(String constant) {
        String lower = constant.replace('_', '-').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
