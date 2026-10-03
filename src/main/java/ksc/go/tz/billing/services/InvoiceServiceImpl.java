package ksc.go.tz.billing.services;

import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.common.pdf.BillTo;
import ksc.go.tz.common.pdf.BillingDocument;
import ksc.go.tz.common.pdf.BillingPdfRenderer;
import java.util.ArrayList;
import afriUtils.responses.AfriException;
import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.billing.repository.InvoiceRepository;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.common.ReferenceNumberGenerator;
import ksc.go.tz.quotation.entities.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    /** Status given to newly generated invoices. */
    public static final String STATUS_PENDING = "PENDING";

    /** Status given to invoices superseded by a regenerated one. */
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** Some, but not all, of the amount due has been received. */
    public static final String STATUS_PARTIALLY_PAID = "PARTIALLY_PAID";

    /** The full amount due has been received. */
    public static final String STATUS_PAID = "PAID";

    /** Number of days the client has to pay a generated invoice. */
    private static final int PAYMENT_TERMS_DAYS = 14;

    private final InvoiceRepository invoiceRepository;
    private final BillingPdfRenderer billingPdfRenderer;

    @Override
    public Invoice generateForQuote(Quote quote, UUID createdBy) {
        LocalDate today = LocalDate.now();
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(ReferenceNumberGenerator.next("INV"));
        invoice.setQuote(quote);
        invoice.setSite(quote.getSite());
        invoice.setItems(quote.getItems().stream().map(LineItem::copy).toList());
        invoice.setAmountDue(quote.getPriceMax());
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setIssueDate(today);
        invoice.setDueDate(today.plusDays(PAYMENT_TERMS_DAYS));
        invoice.setStatus(STATUS_PENDING);
        invoice.setCreatedBy(createdBy);
        invoice.setCreatedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getAll(UUID userId) {
        return invoiceRepository.findAll().stream().map(InvoiceResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceResponseDto> getById(String invoiceId) {
        return invoiceRepository.findById(parseId(invoiceId, "invoice")).map(InvoiceResponseDto::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getBySiteId(String siteId) {
        return invoiceRepository.findBySiteId(parseId(siteId, "site")).stream().map(InvoiceResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findSettledForSite(UUID siteId) {
        return invoiceRepository.findBySiteId(siteId).stream()
                .filter(i -> !STATUS_PENDING.equals(i.getStatus()) && !STATUS_CANCELLED.equals(i.getStatus()))
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Invoice> findCurrentForSites(Collection<UUID> siteIds) {
        Map<UUID, Invoice> current = new HashMap<>();
        if (siteIds.isEmpty()) {
            return current;
        }
        for (Invoice invoice : invoiceRepository.findBySiteIdInOrderByCreatedAtDesc(siteIds)) {
            if (!STATUS_CANCELLED.equals(invoice.getStatus()) && invoice.getSite() != null) {
                current.putIfAbsent(invoice.getSite().getId(), invoice);
            }
        }
        return current;
    }

    @Override
    public void attachContract(Quote quote, Contract contract) {
        for (Invoice invoice : invoiceRepository.findByQuoteId(quote.getId())) {
            if (invoice.getContract() == null && !STATUS_CANCELLED.equals(invoice.getStatus())) {
                invoice.setContract(contract);
                invoice.setUpdatedAt(LocalDateTime.now());
                invoiceRepository.save(invoice);
            }
        }
    }

    @Override
    public void cancelPendingInvoices(UUID siteId, UUID userId) {
        for (Invoice invoice : invoiceRepository.findBySiteId(siteId)) {
            if (STATUS_PENDING.equals(invoice.getStatus())) {
                invoice.setStatus(STATUS_CANCELLED);
                invoice.setUpdatedBy(userId);
                invoice.setUpdatedAt(LocalDateTime.now());
                invoiceRepository.save(invoice);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetaData generatePdf(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(parseId(invoiceId, "invoice"))
                .orElseThrow(() -> new AfriException("Invoice not found"));

        String number = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-" + invoice.getId();
        List<String> notes = new ArrayList<>();
        if (invoice.getDueDate() != null) {
            notes.add("Payment is due by " + BillingPdfRenderer.format(invoice.getDueDate()) + ".");
        }
        notes.add("Please quote invoice number " + number + " with your payment.");
        if (invoice.getQuote() != null && invoice.getQuote().getQuoteNumber() != null) {
            notes.add("Issued against quotation " + invoice.getQuote().getQuoteNumber() + ".");
        }
        notes.add("All amounts are in Tanzanian Shillings (TZS).");

        BillingDocument document = new BillingDocument(
                "INVOICE",
                number,
                invoice.getIssueDate(),
                "Due date",
                invoice.getDueDate(),
                invoice.getStatus() != null ? invoice.getStatus() : "-",
                invoice.getSite() != null ? BillTo.forSite(invoice.getSite()) : List.of("-"),
                invoice.getItems(),
                invoice.getAmountDue(),
                notes
        );
        return new FileMetaData(billingPdfRenderer.render(document), "application/pdf", number + ".pdf");
    }

    private UUID parseId(String id, String label) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AfriException("Invalid " + label + " ID: " + id);
        }
    }
}
