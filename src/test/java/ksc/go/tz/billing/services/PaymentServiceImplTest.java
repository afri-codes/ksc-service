package ksc.go.tz.billing.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.billing.entities.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import ksc.go.tz.billing.client.CreatePaymentRequest;
import ksc.go.tz.billing.client.PaymentServiceClient;
import ksc.go.tz.billing.client.PaymentServiceProperties;
import ksc.go.tz.billing.dto.PaymentRequestDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.billing.kafka.PaymentResultMessage;
import ksc.go.tz.billing.repository.InvoiceRepository;
import ksc.go.tz.billing.repository.PaymentRepository;
import ksc.go.tz.enums.PaymentMethod;
import ksc.go.tz.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private final UUID paymentId = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentRepository paymentRepository;
    private InvoiceRepository invoiceRepository;
    private PaymentServiceClient paymentServiceClient;
    private PaymentServiceImpl service;

    private Invoice invoice;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);
        paymentServiceClient = mock(PaymentServiceClient.class);
        service = new PaymentServiceImpl(paymentRepository, invoiceRepository, paymentServiceClient,
                new PaymentServiceProperties(), mock(PlatformTransactionManager.class), objectMapper, mock(ReceiptPdfRenderer.class));

        invoice = new Invoice();
        invoice.setInvoiceNumber("INV-1");
        invoice.setAmountDue(new BigDecimal("100000"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setStatus(InvoiceServiceImpl.STATUS_PENDING);

        payment = new Payment();
        payment.setId(paymentId);
        payment.setInvoice(invoice);
        payment.setAmount(new BigDecimal("100000"));
        payment.setMethod(PaymentMethod.MOBILE_MONEY);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    }

    private PaymentResultMessage result(String status, String amount, String failureReason) {
        return new PaymentResultMessage("evt-1", "KSC", paymentId.toString(), null, status,
                amount == null ? null : new BigDecimal(amount), "TZS", "MP123", "MOBILE_MONEY",
                "SUCCESS".equals(status) ? LocalDateTime.of(2026, 10, 2, 9, 0) : null, failureReason);
    }

    @Test
    void successPaysTheInvoice() {
        service.handleResult(result("SUCCESS", "100000", null));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("MP123", payment.getProviderRef());
        assertEquals(LocalDateTime.of(2026, 10, 2, 9, 0), payment.getPaidAt());
        assertEquals(InvoiceServiceImpl.STATUS_PAID, invoice.getStatus());
        assertEquals(0, new BigDecimal("100000").compareTo(invoice.getAmountPaid()));
    }

    @Test
    void duplicateSuccessIsIgnored() {
        service.handleResult(result("SUCCESS", "100000", null));
        service.handleResult(result("SUCCESS", "100000", null));

        assertEquals(0, new BigDecimal("100000").compareTo(invoice.getAmountPaid()));
    }

    @Test
    void failureRecordsTheReason() {
        service.handleResult(result("FAILED", "100000", "Insufficient balance"));

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("Insufficient balance", payment.getFailureReason());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void lateSuccessAfterFailureIsRecorded() {
        service.handleResult(result("FAILED", "100000", "Timed out"));
        service.handleResult(result("SUCCESS", "100000", null));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNull(payment.getFailureReason());
        assertEquals(InvoiceServiceImpl.STATUS_PAID, invoice.getStatus());
    }

    @Test
    void lateSuccessOnSettledInvoiceDoesNotOverpay() {
        payment.setStatus(PaymentStatus.FAILED);
        // Staff collected the balance in cash after the payment failed
        invoice.setAmountPaid(new BigDecimal("100000"));
        invoice.setStatus(InvoiceServiceImpl.STATUS_PAID);

        service.handleResult(result("SUCCESS", "100000", null));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(0, new BigDecimal("100000").compareTo(invoice.getAmountPaid()));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void lateSuccessOnlyFillsTheRemainingBalance() {
        payment.setStatus(PaymentStatus.CANCELLED);
        invoice.setAmountPaid(new BigDecimal("60000"));
        invoice.setStatus(InvoiceServiceImpl.STATUS_PARTIALLY_PAID);

        service.handleResult(result("SUCCESS", "100000", null));

        assertEquals(0, new BigDecimal("100000").compareTo(invoice.getAmountPaid()));
        assertEquals(InvoiceServiceImpl.STATUS_PAID, invoice.getStatus());
    }

    @Test
    void failureAfterFailureIsIgnored() {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("first");

        service.handleResult(result("CANCELLED", "100000", "second"));

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("first", payment.getFailureReason());
    }

    // --- initiatePayment: calls the payment service API

    private void payableInvoice() {
        UUID invoiceId = UUID.randomUUID();
        invoice.setId(invoiceId);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.existsByInvoiceIdAndStatusIn(any(), any())).thenReturn(false);
        // save() assigns the id like the database would, and later reads return the same row
        doAnswer(inv -> {
            Payment saved = inv.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(paymentId);
            }
            payment = saved;
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(saved));
            return saved;
        }).when(paymentRepository).save(any(Payment.class));
    }

    private PaymentResponseDto pay(PaymentMethod method) {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setMethod(method);
        request.setPayerPhone("0712345678");
        return service.initiatePayment(invoice.getId().toString(), request, UUID.randomUUID(), "Bearer t");
    }

    private void paymentServiceAnswers(String status) throws Exception {
        when(paymentServiceClient.createPayment(anyString(), anyString(), any()))
                .thenReturn(objectMapper.readTree("{\"status\":\"SUCCESS\",\"data\":{\"status\":\"" + status + "\"}}"));
    }

    private static FeignException httpError(int status, String body) {
        Request request = Request.create(Request.HttpMethod.POST, "http://payment-service/api/v2/payments/CLICKPESA",
                new HashMap<>(), null, StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("createPayment", feign.Response.builder()
                .status(status).reason("error").request(request).headers(new HashMap<>())
                .body(body, StandardCharsets.UTF_8).build());
    }

    @Test
    void acceptedPaymentIsProcessingAndSentWithItsId() throws Exception {
        payableInvoice();
        paymentServiceAnswers("PROCESSING");

        pay(PaymentMethod.MOBILE_MONEY);

        ArgumentCaptor<CreatePaymentRequest> sent = ArgumentCaptor.forClass(CreatePaymentRequest.class);
        verify(paymentServiceClient).createPayment(eq("Bearer t"), eq("CLICKPESA"), sent.capture());
        assertEquals("KSC", sent.getValue().system());
        assertEquals(paymentId.toString(), sent.getValue().reference());
        assertEquals("MOBILE_MONEY", sent.getValue().method());
        assertEquals("0712345678", sent.getValue().phoneNumber());
        assertEquals("KSC invoice INV-1", sent.getValue().customerName());

        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
    }

    @Test
    void rejectedPaymentIsFailedWithTheReason() {
        payableInvoice();
        when(paymentServiceClient.createPayment(anyString(), anyString(), any())).thenThrow(httpError(400,
                "{\"status\":400,\"error\":\"PAYMENT_GATEWAY_ERROR\",\"message\":\"M-Pesa payment method is not active\"}"));

        pay(PaymentMethod.MOBILE_MONEY);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("M-Pesa payment method is not active", payment.getFailureReason());
    }

    @Test
    void unconfirmedPaymentStaysInProgress() {
        payableInvoice();
        when(paymentServiceClient.createPayment(anyString(), anyString(), any())).thenThrow(httpError(503, ""));

        pay(PaymentMethod.MOBILE_MONEY);

        // The prompt may have reached the payer: keep the invoice blocked until a result arrives
        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
        assertNull(payment.getFailureReason());
    }

    @Test
    void resultAlreadyReceivedFromKafkaIsNotOverwritten() {
        payableInvoice();
        when(paymentServiceClient.createPayment(anyString(), anyString(), any())).thenAnswer(inv -> {
            // SUCCESS arrives on Kafka before the API call returns
            service.handleResult(result("SUCCESS", "100000", null));
            return objectMapper.readTree("{\"data\":{\"status\":\"PROCESSING\"}}");
        });

        pay(PaymentMethod.MOBILE_MONEY);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
    }

    @Test
    void cardGoesThroughCheckoutAndKeepsTheLink() throws Exception {
        payableInvoice();
        when(paymentServiceClient.createPayment(anyString(), anyString(), any())).thenReturn(objectMapper.readTree(
                "{\"data\":{\"status\":\"PENDING\",\"paymentReference\":\"PAYS1\",\"paymentUrl\":\"https://pay.example/abc\"}}"));

        pay(PaymentMethod.CARD);

        ArgumentCaptor<CreatePaymentRequest> sent = ArgumentCaptor.forClass(CreatePaymentRequest.class);
        verify(paymentServiceClient).createPayment(anyString(), anyString(), sent.capture());
        assertEquals("CHECKOUT", sent.getValue().method());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals("https://pay.example/abc", payment.getPaymentUrl());
        assertEquals("PAYS1", payment.getPaymentReference());
    }

    @Test
    void bankGoesThroughBillPayAndKeepsTheControlNumber() throws Exception {
        payableInvoice();
        when(paymentServiceClient.createPayment(anyString(), anyString(), any())).thenReturn(objectMapper.readTree(
                "{\"data\":{\"status\":\"PENDING\",\"controlNumber\":\"55042914871931\"}}"));

        pay(PaymentMethod.BANK);

        ArgumentCaptor<CreatePaymentRequest> sent = ArgumentCaptor.forClass(CreatePaymentRequest.class);
        verify(paymentServiceClient).createPayment(anyString(), anyString(), sent.capture());
        assertEquals("BILLPAY", sent.getValue().method());
        assertEquals("55042914871931", payment.getControlNumber());
    }

    @Test
    void methodsMapToThePaymentServiceMethods() {
        assertEquals("MOBILE_MONEY", PaymentServiceImpl.gatewayMethod(PaymentMethod.MOBILE_MONEY));
        assertEquals("CHECKOUT", PaymentServiceImpl.gatewayMethod(PaymentMethod.CHECKOUT));
        assertEquals("CHECKOUT", PaymentServiceImpl.gatewayMethod(PaymentMethod.CARD));
        assertEquals("BILLPAY", PaymentServiceImpl.gatewayMethod(PaymentMethod.BILLPAY));
        assertEquals("BILLPAY", PaymentServiceImpl.gatewayMethod(PaymentMethod.BANK));
    }

    @Test
    void unsupportedMethodsNeverReachThePaymentService() {
        payableInvoice();

        assertThrows(AfriException.class, () -> pay(PaymentMethod.PAYPAL));
        assertThrows(AfriException.class, () -> pay(PaymentMethod.STRIPE));
        assertThrows(AfriException.class, () -> pay(PaymentMethod.CASH));

        verify(paymentServiceClient, never()).createPayment(anyString(), anyString(), any());
    }

    @Test
    void successAssignsAReceiptNumber() {
        service.handleResult(result("SUCCESS", "100000", null));

        assertTrue(payment.getReceiptNumber().startsWith("RCT-"));
    }

    @Test
    void receiptIsRefusedUntilThePaymentSucceeds() {
        payment.setStatus(PaymentStatus.PROCESSING);

        AfriException e = assertThrows(AfriException.class, () -> service.generateReceipt(paymentId.toString()));
        assertTrue(e.getMessage().contains("only available once it has succeeded"));
    }

    @Test
    void oldSuccessfulPaymentGetsAReceiptNumberOnFirstRequest() {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setReceiptNumber(null);

        service.generateReceipt(paymentId.toString());

        assertTrue(payment.getReceiptNumber().startsWith("RCT-"));
    }
}
