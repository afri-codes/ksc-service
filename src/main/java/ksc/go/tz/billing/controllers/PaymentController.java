package ksc.go.tz.billing.controllers;

import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayInputStream;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ksc.go.tz.billing.dto.CashPaymentDto;
import ksc.go.tz.billing.dto.PaymentRequestDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.billing.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "Payments", description = "Invoice payments collected through the payment service; results arrive on Kafka")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    @Operation(summary = "Pay an invoice", description = "Starts a payment through the payment service. method: MOBILE_MONEY (PIN prompt on payerPhone), "
            + "CARD or CHECKOUT (open the returned paymentUrl), BILLPAY or BANK (show the returned controlNumber). CASH uses the cash endpoint; PAYPAL, STRIPE and OTHER are not supported. "
            + "The invoice becomes PAID (or PARTIALLY_PAID) when the payment service reports SUCCESS. Amount defaults to the remaining balance. Only one payment per invoice can be in progress at a time.")
    @Permission(name="PAY INVOICE", code = "PAY_INVOICE")
    @PostMapping("/invoices/{invoiceId}/payments")
    public ApiResponseUtil.ApiResponseEntity<PaymentResponseDto> payInvoice(@PathVariable("invoiceId") String invoiceId,
                                                                            @RequestBody @Valid PaymentRequestDto paymentRequestDto,
                                                                            Authentication authentication) {
        PaymentResponseDto payment = paymentService.initiatePayment(invoiceId, paymentRequestDto,
                authDetailsExtractor.getUserId(authentication), authDetailsExtractor.getBearerToken(authentication));
        return apiResponseUtil.getResponse(null, payment, "Payment request sent", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Record a cash payment", description = "Records cash already received for an invoice. The payment is saved as SUCCESS "
            + "immediately (no payment service involved) and the invoice moves to PARTIALLY_PAID or PAID. "
            + "Amount defaults to the remaining balance and cannot exceed it. Not allowed while another payment for the invoice is in progress.")
    @Permission(name="RECORD CASH PAYMENT", code = "RECORD_CASH_PAYMENT")
    @PostMapping("/invoices/{invoiceId}/payments/cash")
    public ApiResponseUtil.ApiResponseEntity<PaymentResponseDto> recordCashPayment(@PathVariable("invoiceId") String invoiceId,
                                                                                   @RequestBody @Valid CashPaymentDto cashPaymentDto,
                                                                                   Authentication authentication) {
        PaymentResponseDto payment = paymentService.recordCashPayment(invoiceId, cashPaymentDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, payment, "Cash payment recorded", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get payments for an invoice ")
    @Permission(name="VIEW INVOICE PAYMENTS", code = "VIEW_INVOICE_PAYMENTS")
    @GetMapping("/invoices/{invoiceId}/payments")
    public ApiResponseUtil.ApiResponseEntity<List<PaymentResponseDto>> getByInvoice(@PathVariable("invoiceId") String invoiceId) {
        return apiResponseUtil.getResponse(paymentService.getByInvoiceId(invoiceId));
    }

    @Operation(summary = "get all payment list ")
    @Permission(name="VIEW ALL PAYMENT", code = "VIEW_PAYMENT")
    @GetMapping("/payments")
    public ApiResponseUtil.ApiResponseEntity<List<PaymentResponseDto>> getAll(Authentication authentication) {
        return apiResponseUtil.getResponse(paymentService.getAll(authDetailsExtractor.getUserId(authentication)));
    }

    @Operation(summary = "get payment by id ", description = "Use this to poll a payment's status after paying an invoice.")
    @Permission(name="VIEW PAYMENT BY ID", code = "VIEW_PAYMENT_BY_ID")
    @GetMapping("/payments/{id}")
    public ApiResponseUtil.ApiResponseEntity<PaymentResponseDto> getById(@PathVariable("id") String paymentId) {
        Optional<PaymentResponseDto> payment = paymentService.getById(paymentId);
        if (payment.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Payment not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(payment.get());
    }

    @Operation(summary = "Download payment receipt", description = "PDF receipt for a SUCCESS payment: amount received, method and references, "
            + "and the invoice's total, paid-to-date and remaining balance. Other statuses are rejected.")
    @Permission(name="DOWNLOAD PAYMENT RECEIPT", code = "DOWNLOAD_PAYMENT_RECEIPT")
    @GetMapping(value = "/payments/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> downloadReceipt(@PathVariable("id") String paymentId) {
        FileMetaData pdf = paymentService.generateReceipt(paymentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(pdf.getFileName()).build());
        headers.setContentLength(pdf.getBytes().length);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(new ByteArrayInputStream(pdf.getBytes())));
    }

}
