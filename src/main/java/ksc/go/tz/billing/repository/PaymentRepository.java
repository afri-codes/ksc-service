package ksc.go.tz.billing.repository;

import ksc.go.tz.billing.entities.Payment;
import ksc.go.tz.enums.PaymentMethod;
import ksc.go.tz.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByInvoiceIdOrderByCreatedAtDesc(UUID invoiceId);

    List<Payment> findByInvoiceSiteIdOrderByCreatedAtDesc(UUID siteId);

    List<Payment> findByInvoiceContractIdOrderByCreatedAtDesc(UUID contractId);

    List<Payment> findByStatusInAndMethodAndCreatedAtBefore(Collection<PaymentStatus> statuses, PaymentMethod method, LocalDateTime before);

    List<Payment> findByStatusInAndMethodNotAndCreatedAtBefore(Collection<PaymentStatus> statuses, PaymentMethod method, LocalDateTime before);

    /** Payments still waiting on the payment service since before {@code createdBefore}. */

    boolean existsByInvoiceIdAndStatusIn(UUID invoiceId, Collection<PaymentStatus> statuses);

    boolean existsByInvoiceSiteIdAndStatusIn(UUID siteId, Collection<PaymentStatus> statuses);

    boolean existsByInvoiceIdAndMethodAndProviderRefIgnoreCase(UUID invoiceId, PaymentMethod method, String providerRef);
}
