package ksc.go.tz.billing.services;

import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import java.util.Collection;
import java.util.Map;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.quotation.entities.Quote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceService {

    Invoice generateForQuote(Quote quote, UUID createdBy);

    List<InvoiceResponseDto> getAll(UUID userId);

    Optional<InvoiceResponseDto> getById(String invoiceId);

    List<InvoiceResponseDto> getBySiteId(String siteId);

    FileMetaData generatePdf(String invoiceId);

    /** A site invoice that has moved past PENDING (e.g. paid) and so locks the site's pricing, if any. */
    Optional<Invoice> findSettledForSite(UUID siteId);

    /** Current invoice (latest not cancelled) per site, for the given sites; sites without one are absent. */
    Map<UUID, Invoice> findCurrentForSites(Collection<UUID> siteIds);

    /** Links the quote's non-cancelled invoices that have no contract yet to this contract. */
    void attachContract(Quote quote, Contract contract);

    /** Marks the site's PENDING invoices as CANCELLED. */
    void cancelPendingInvoices(UUID siteId, UUID userId);
}
