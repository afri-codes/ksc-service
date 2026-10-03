package ksc.go.tz.billing.services;

import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.common.ReferenceNumberGenerator;
import afriUtils.responses.AfriException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import ksc.go.tz.billing.client.CreatePaymentRequest;
import ksc.go.tz.billing.client.PaymentServiceClient;
import ksc.go.tz.billing.client.PaymentServiceProperties;
import ksc.go.tz.billing.dto.CashPaymentDto;
import ksc.go.tz.billing.dto.PaymentRequestDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.billing.entities.Payment;
import ksc.go.tz.billing.kafka.PaymentResultMessage;
import ksc.go.tz.billing.repository.InvoiceRepository;
import ksc.go.tz.billing.repository.PaymentRepository;
import ksc.go.tz.enums.PaymentMethod;
import ksc.go.tz.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY = "TZS";

    /** Payments still waiting on the payment service (also what PaymentResultTimeoutJob releases). */
    static final Set<PaymentStatus> IN_PROGRESS = EnumSet.of(PaymentStatus.CREATED, PaymentStatus.PENDING, PaymentStatus.PROCESSING);

    /** Payments whose outcome is settled; later results for them are ignored. */
    private static final Set<PaymentStatus> FINAL = EnumSet.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED,
            PaymentStatus.CANCELLED, PaymentStatus.REFUND_PENDING, PaymentStatus.REFUNDED);

    /** Settled-as-unpaid statuses a late SUCCESS may still override. */
    private static final Set<PaymentStatus> LATE_SUCCESS_FROM = EnumSet.of(PaymentStatus.FAILED, PaymentStatus.CANCELLED);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final PaymentServiceProperties paymentServiceProperties;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;
    private final ReceiptPdfRenderer receiptPdfRenderer;

    /**
     * Saves the payment, asks the payment service to collect it, then records how the
     * request went. The HTTP call runs outside any transaction so a slow payment service
     * never holds a database connection, and the payment row exists before the call.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResponseDto initiatePayment(String invoiceId, PaymentRequestDto request, UUID userId, String authorization) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        CreatePaymentRequest paymentRequest = transaction.execute(status -> createPendingPayment(invoiceId, request, userId));
        UUID paymentId = UUID.fromString(paymentRequest.reference());

        DispatchOutcome outcome = dispatch(paymentRequest, authorization);

        return transaction.execute(status -> applyDispatchOutcome(paymentId, outcome));
    }

    private CreatePaymentRequest createPendingPayment(String invoiceId, PaymentRequestDto request, UUID userId) {
        Invoice invoice = findPayableInvoice(invoiceId);
        if (request.getMethod() == PaymentMethod.CASH) {
            throw new AfriException("Cash payments are recorded with POST /invoices/{invoiceId}/payments/cash.");
        }
        String gatewayMethod = gatewayMethod(request.getMethod());
        if (request.getPayerPhone() == null || request.getPayerPhone().isBlank()) {
            throw new AfriException("Payer phone number is required: the payment service needs it for every method.");
        }

        BigDecimal amount = resolveAmount(invoice, request.getAmount());

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setCurrency(CURRENCY);
        payment.setMethod(request.getMethod());
        payment.setPayerPhone(request.getPayerPhone());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedBy(userId);
        payment.setCreatedAt(now);
        Payment saved = paymentRepository.save(payment);

        String payerName = request.getPayerName() != null && !request.getPayerName().isBlank()
                ? request.getPayerName().trim()
                : "KSC invoice " + invoice.getInvoiceNumber();
        String payerEmail = request.getPayerEmail() != null && !request.getPayerEmail().isBlank() ? request.getPayerEmail().trim() : null;
        String phone = request.getPayerPhone().trim();

        return new CreatePaymentRequest(
                paymentServiceProperties.getSystem(),
                saved.getId().toString(),
                gatewayMethod,
                amount,
                CURRENCY,
                "Payment for invoice " + invoice.getInvoiceNumber(),
                payerName,
                payerEmail,
                phone,
                phone,
                new CreatePaymentRequest.Customer(payerName, phone, payerEmail)
        );
    }

    /** How the payment service answered the request. */
    /**
     * The payment service method for a KSC method. The payment service only takes CARD in USD, so KSC's
     * TZS card payments go through CHECKOUT (a hosted page where the client can pay by card); BANK uses
     * BILLPAY, whose control number can be paid at a bank.
     */
    static String gatewayMethod(PaymentMethod method) {
        return switch (method) {
            case MOBILE_MONEY -> "MOBILE_MONEY";
            case CARD, CHECKOUT -> "CHECKOUT";
            case BILLPAY, BANK -> "BILLPAY";
            case CASH -> throw new AfriException("Cash payments are recorded with POST /invoices/{invoiceId}/payments/cash.");
            default -> throw new AfriException(method.getDisplayName()
                    + " is not supported. Use MOBILE_MONEY, CARD, CHECKOUT, BILLPAY or BANK, or record CASH.");
        };
    }

    /** How the payment service answered, plus what the client needs to complete the payment. */
    private record DispatchOutcome(PaymentStatus status, String failureReason,
                                   String paymentReference, String paymentUrl, String controlNumber) {
        DispatchOutcome(PaymentStatus status, String failureReason) {
            this(status, failureReason, null, null, null);
        }
    }

    private DispatchOutcome dispatch(CreatePaymentRequest request, String authorization) {
        log.info("[PAYMENT] Sending paymentId={} amount={} to the payment service ({})",
                request.reference(), request.amount(), paymentServiceProperties.getProvider());
        try {
            JsonNode data = paymentServiceClient
                    .createPayment(authorization, paymentServiceProperties.getProvider(), request)
                    .path("data");
            String status = data.path("status").asText("");
            String paymentReference = textOrNull(data, "paymentReference");
            log.info("[PAYMENT] Payment service accepted paymentId={} method={} status={} paymentReference={}",
                    request.reference(), request.method(), status, paymentReference);
            if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                return new DispatchOutcome(PaymentStatus.FAILED, data.path("message").asText("Rejected by the payment service"),
                        paymentReference, null, null);
            }
            // CREATED/PENDING: waiting for the client (link or control number); PROCESSING: prompt sent.
            PaymentStatus inProgress = "PROCESSING".equals(status) ? PaymentStatus.PROCESSING : PaymentStatus.PENDING;
            return new DispatchOutcome(inProgress, null, paymentReference,
                    textOrNull(data, "paymentUrl"), textOrNull(data, "controlNumber"));
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                String reason = errorMessage(e);
                log.warn("[PAYMENT] Payment service rejected paymentId={}: {}", request.reference(), reason);
                return new DispatchOutcome(PaymentStatus.FAILED, reason);
            }
            return outcomeUnknown(request, e);
        } catch (Exception e) {
            return outcomeUnknown(request, e);
        }
    }

    /**
     * Timeout or server error: the prompt may already be on the payer's phone. Keep the
     * payment in progress (so nobody starts a second one) and wait for the result on Kafka;
     * PaymentResultTimeoutJob fails it if none ever comes.
     */
    private DispatchOutcome outcomeUnknown(CreatePaymentRequest request, Exception e) {
        log.error("[PAYMENT] Payment service did not confirm paymentId={}; waiting for its result: {}",
                request.reference(), e.getMessage());
        return new DispatchOutcome(PaymentStatus.PROCESSING, null);
    }

    private PaymentResponseDto applyDispatchOutcome(UUID paymentId, DispatchOutcome outcome) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AfriException("Payment not found"));
        // A result from Kafka may already have arrived; never overwrite it
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(outcome.status());
            payment.setFailureReason(outcome.failureReason());
            payment.setUpdatedAt(LocalDateTime.now());
        }
        // The link / control number are needed even if a Kafka result raced ahead.
        if (outcome.paymentReference() != null) {
            payment.setPaymentReference(outcome.paymentReference());
        }
        if (outcome.paymentUrl() != null) {
            payment.setPaymentUrl(outcome.paymentUrl());
        }
        if (outcome.controlNumber() != null) {
            payment.setControlNumber(outcome.controlNumber());
        }
        payment = paymentRepository.save(payment);
        return new PaymentResponseDto(payment);
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    /** The payment service's error message (its 400 body), or the HTTP status when there is none. */
    private String errorMessage(FeignException e) {
        try {
            JsonNode body = objectMapper.readTree(e.contentUTF8());
            if (body.hasNonNull("message")) {
                return body.get("message").asText();
            }
        } catch (Exception ignored) {
            // Not JSON
        }
        return "Payment service returned HTTP " + e.status();
    }

    @Override
    public PaymentResponseDto recordCashPayment(String invoiceId, CashPaymentDto cash, UUID userId) {
        Invoice invoice = findPayableInvoice(invoiceId);
        String receiptNumber = cash.getReceiptNumber().trim();
        if (paymentRepository.existsByInvoiceIdAndMethodAndProviderRefIgnoreCase(invoice.getId(), PaymentMethod.CASH, receiptNumber)) {
            throw new AfriException("Receipt " + receiptNumber + " is already recorded for invoice " + invoice.getInvoiceNumber() + ".");
        }
        BigDecimal amount = resolveAmount(invoice, cash.getAmount());

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setCurrency(CURRENCY);
        payment.setMethod(PaymentMethod.CASH);
        payment.setProviderRef(receiptNumber);
        payment.setNotes(cash.getNotes());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setReceiptNumber(ReferenceNumberGenerator.next("RCT"));
        payment.setPaidAt(cash.getReceivedAt() != null ? cash.getReceivedAt() : now);
        payment.setCreatedBy(userId);
        payment.setCreatedAt(now);
        Payment saved = paymentRepository.save(payment);

        applyToInvoice(invoice, amount, userId, now);
        log.info("[PAYMENT] Cash payment {} of {} recorded for invoice {} by {} (receipt {})",
                saved.getId(), amount, invoice.getInvoiceNumber(), userId, saved.getProviderRef());
        return new PaymentResponseDto(saved);
    }

    @Override
    public FileMetaData generateReceipt(String paymentId) {
        Payment payment = paymentRepository.findById(parseId(paymentId, "payment"))
                .orElseThrow(() -> new AfriException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new AfriException("Payment is " + payment.getStatus() + "; a receipt is only available once it has succeeded.");
        }
        if (payment.getReceiptNumber() == null) {
            // Payments that succeeded before receipts existed get their number on first request.
            payment.setReceiptNumber(ReferenceNumberGenerator.next("RCT"));
            paymentRepository.save(payment);
        }
        return new FileMetaData(receiptPdfRenderer.render(payment), "application/pdf", payment.getReceiptNumber() + ".pdf");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAll(UUID userId) {
        return paymentRepository.findAll().stream().map(PaymentResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponseDto> getById(String paymentId) {
        return paymentRepository.findById(parseId(paymentId, "payment")).map(PaymentResponseDto::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getByInvoiceId(String invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByCreatedAtDesc(parseId(invoiceId, "invoice")).stream()
                .map(PaymentResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getBySiteId(UUID siteId) {
        return paymentRepository.findByInvoiceSiteIdOrderByCreatedAtDesc(siteId).stream()
                .map(PaymentResponseDto::new).toList();
    }

    @Override
    public void handleResult(PaymentResultMessage result) {
        Payment payment = paymentRepository.findById(parseId(result.paymentId(), "payment")).orElse(null);
        if (payment == null) {
            log.warn("[PAYMENT] Result for unknown paymentId={} invoice={} ignored", result.paymentId(), result.invoiceNumber());
            return;
        }

        PaymentStatus newStatus;
        try {
            newStatus = PaymentStatus.valueOf(result.status() == null ? "" : result.status().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[PAYMENT] Result with unknown status '{}' for paymentId={} ignored", result.status(), result.paymentId());
            return;
        }

        // Money can arrive after the payment service gave up on a payment, so a SUCCESS may
        // still follow FAILED/CANCELLED. Anything else for a settled payment is a redelivery.
        boolean lateSuccess = newStatus == PaymentStatus.SUCCESS && LATE_SUCCESS_FROM.contains(payment.getStatus());
        if (FINAL.contains(payment.getStatus()) && !lateSuccess) {
            // Kafka can redeliver; a settled payment must never be applied twice.
            log.info("[PAYMENT] paymentId={} already {}; result {} ignored", payment.getId(), payment.getStatus(), newStatus);
            return;
        }
        if (lateSuccess) {
            log.warn("[PAYMENT] paymentId={} was {} but the payment service now reports SUCCESS; recording it",
                    payment.getId(), payment.getStatus());
            payment.setFailureReason(null);
        }

        LocalDateTime now = LocalDateTime.now();
        if (result.providerRef() != null) {
            payment.setProviderRef(result.providerRef());
        }
        payment.setUpdatedAt(now);

        switch (newStatus) {
            case PROCESSING -> payment.setStatus(PaymentStatus.PROCESSING);
            case SUCCESS -> applySuccess(payment, result, now);
            case FAILED, CANCELLED -> {
                payment.setStatus(newStatus);
                payment.setFailureReason(result.failureReason());
                log.info("[PAYMENT] paymentId={} {}: {}", payment.getId(), newStatus, result.failureReason());
            }
            default -> {
                log.warn("[PAYMENT] Status {} is not a valid result for paymentId={}; ignored", newStatus, payment.getId());
                return;
            }
        }
        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPaymentInProgressForSite(UUID siteId) {
        return paymentRepository.existsByInvoiceSiteIdAndStatusIn(siteId, IN_PROGRESS);
    }

    private void applySuccess(Payment payment, PaymentResultMessage result, LocalDateTime now) {
        BigDecimal received = result.amount() != null ? result.amount() : payment.getAmount();
        if (received.compareTo(payment.getAmount()) != 0) {
            log.warn("[PAYMENT] paymentId={} requested {} but payment service reports {}; recording the reported amount",
                    payment.getId(), payment.getAmount(), received);
        }
        payment.setAmount(received);
        payment.setStatus(PaymentStatus.SUCCESS);
        if (payment.getReceiptNumber() == null) {
            payment.setReceiptNumber(ReferenceNumberGenerator.next("RCT"));
        }
        payment.setPaidAt(result.paidAt() != null ? result.paidAt() : now);
        if (result.method() != null) {
            try {
                payment.setMethod(PaymentMethod.valueOf(result.method().trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // Keep the requested method if the payment service sends one we don't know.
            }
        }

        Invoice invoice = payment.getInvoice();
        if (InvoiceServiceImpl.STATUS_CANCELLED.equals(invoice.getStatus())) {
            log.error("[PAYMENT] paymentId={} succeeded for CANCELLED invoice {}; recorded, but the client may need a refund",
                    payment.getId(), invoice.getInvoiceNumber());
            return;
        }
        // Only count what the invoice still owes: a late SUCCESS can arrive after staff
        // settled the invoice another way (new payment or cash).
        BigDecimal balance = balanceDue(invoice);
        BigDecimal applied = received.min(balance.max(BigDecimal.ZERO));
        if (applied.compareTo(received) < 0) {
            log.error("[PAYMENT] paymentId={} received {} but invoice {} only owed {}; {} needs a refund",
                    payment.getId(), received, invoice.getInvoiceNumber(), balance.max(BigDecimal.ZERO),
                    received.subtract(applied));
        }
        if (applied.signum() > 0) {
            applyToInvoice(invoice, applied, null, now);
        }
    }

    /** Adds a received amount to the invoice and moves it to PARTIALLY_PAID or PAID. */
    private void applyToInvoice(Invoice invoice, BigDecimal received, UUID userId, LocalDateTime now) {
        BigDecimal paid = (invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO).add(received);
        invoice.setAmountPaid(paid);
        invoice.setStatus(paid.compareTo(invoice.getAmountDue()) >= 0
                ? InvoiceServiceImpl.STATUS_PAID
                : InvoiceServiceImpl.STATUS_PARTIALLY_PAID);
        if (userId != null) {
            invoice.setUpdatedBy(userId);
        }
        invoice.setUpdatedAt(now);
        invoiceRepository.save(invoice);
        log.info("[PAYMENT] Invoice {} is now {} (paid {} of {})",
                invoice.getInvoiceNumber(), invoice.getStatus(), paid, invoice.getAmountDue());
    }

    /** Loads an invoice that can accept a payment right now, or explains why it can't. */
    private Invoice findPayableInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(parseId(invoiceId, "invoice"))
                .orElseThrow(() -> new AfriException("Invoice not found"));
        String status = invoice.getStatus();
        if (!InvoiceServiceImpl.STATUS_PENDING.equals(status) && !InvoiceServiceImpl.STATUS_PARTIALLY_PAID.equals(status)) {
            throw new AfriException("Invoice " + invoice.getInvoiceNumber() + " is " + status + " and cannot take payments.");
        }
        if (paymentRepository.existsByInvoiceIdAndStatusIn(invoice.getId(), IN_PROGRESS)) {
            throw new AfriException("A payment for invoice " + invoice.getInvoiceNumber()
                    + " is already in progress. Wait for its result before recording another.");
        }
        return invoice;
    }

    /** The requested amount, or the remaining balance when none is given; must be positive and within the balance. */
    private static BigDecimal resolveAmount(Invoice invoice, BigDecimal requested) {
        BigDecimal balance = balanceDue(invoice);
        BigDecimal amount = requested != null ? requested : balance;
        if (amount.signum() <= 0) {
            throw new AfriException("Invoice " + invoice.getInvoiceNumber() + " has nothing left to pay.");
        }
        if (amount.compareTo(balance) > 0) {
            throw new AfriException("Amount " + amount.toPlainString() + " is more than the remaining balance of TZS "
                    + balance.toPlainString() + ".");
        }
        return amount;
    }

    private static BigDecimal balanceDue(Invoice invoice) {
        BigDecimal paid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        return invoice.getAmountDue().subtract(paid);
    }

    private static UUID parseId(String id, String label) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AfriException("Invalid " + label + " ID: " + id);
        }
    }
}
