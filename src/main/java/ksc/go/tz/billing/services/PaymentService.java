package ksc.go.tz.billing.services;

import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.billing.dto.CashPaymentDto;
import ksc.go.tz.billing.dto.PaymentRequestDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.billing.kafka.PaymentResultMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    /**
     * Creates the payment and asks the payment service to collect it, passing the caller's
     * bearer token. Returns PROCESSING when the request was accepted, FAILED when rejected.
     */
    PaymentResponseDto initiatePayment(String invoiceId, PaymentRequestDto paymentRequestDto, UUID userId, String authorization);

    /** Records cash already received as a SUCCESS payment and updates the invoice. No payment service involved. */
    PaymentResponseDto recordCashPayment(String invoiceId, CashPaymentDto cashPaymentDto, UUID userId);

    /** PDF receipt for a SUCCESS payment (assigns its receipt number first if it has none). */
    FileMetaData generateReceipt(String paymentId);

    List<PaymentResponseDto> getAll(UUID userId);

    Optional<PaymentResponseDto> getById(String paymentId);

    List<PaymentResponseDto> getByInvoiceId(String invoiceId);

    /** All payments for any of the site's invoices, newest first. */
    List<PaymentResponseDto> getBySiteId(UUID siteId);

    /** Applies an outcome received from the payment service. */
    void handleResult(PaymentResultMessage result);

    /** True when any invoice of the site has a payment waiting on the payment service. */
    boolean hasPaymentInProgressForSite(UUID siteId);
}
