package ksc.go.tz.billing.repository;

import java.util.Collection;
import ksc.go.tz.billing.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findBySiteId(UUID siteId);

    List<Invoice> findByQuoteId(UUID quoteId);

    List<Invoice> findByContractIdOrderByCreatedAtDesc(UUID contractId);

    List<Invoice> findBySiteIdInOrderByCreatedAtDesc(Collection<UUID> siteIds);
}
