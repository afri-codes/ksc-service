package ksc.go.tz.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.billing.entities.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payment details")
public class PaymentResponseDto {

    @Schema(description = "Payment ID (also the correlation ID sent to the payment service)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String paymentId;

    @Schema(description = "Invoice being paid")
    private String invoiceId;

    @Schema(description = "Invoice number", example = "INV-20261002-7K3Q9A")
    private String invoiceNumber;

    @Schema(description = "Amount in TZS", example = "140000.00")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "TZS")
    private String currency;

    @Schema(description = "Payment method", example = "MOBILE_MONEY")
    private String method;

    @Schema(description = "Phone number charged", example = "255712345678")
    private String payerPhone;

    @Schema(description = "PENDING (sent to payment service), PROCESSING, SUCCESS, FAILED or CANCELLED", example = "PENDING")
    private String status;

    @Schema(description = "Provider reference, or the receipt number for cash payments", example = "MPESA-QX81K2")
    private String providerRef;

    @Schema(description = "Why the payment failed or was cancelled")
    private String failureReason;

    @Schema(description = "Staff note (cash payments)")
    private String notes;

    @Schema(description = "KSC receipt number, set once the payment succeeds; download the receipt at GET /payments/{id}/receipt", example = "RCT-20261002-7K3Q9A")
    private String receiptNumber;

    @Schema(description = "Payment service reference for this attempt; quote it to support", example = "PAYS5XADEP6HELUDUY47")
    private String paymentReference;

    @Schema(description = "CARD / CHECKOUT: open this link so the client can pay", example = "https://checkout.clickpesa.com/...")
    private String paymentUrl;

    @Schema(description = "BILLPAY / BANK: control number the client pays via mobile money, bank or agent", example = "55042914871931")
    private String controlNumber;

    @Schema(description = "When the payment was requested")
    private LocalDateTime requestedAt;

    @Schema(description = "When the money was received")
    private LocalDateTime paidAt;

    public PaymentResponseDto(Payment payment) {
        this.paymentId = payment.getId().toString();
        this.invoiceId = payment.getInvoice().getId().toString();
        this.invoiceNumber = payment.getInvoice().getInvoiceNumber();
        this.amount = payment.getAmount();
        this.currency = payment.getCurrency();
        this.method = payment.getMethod() != null ? payment.getMethod().name() : null;
        this.payerPhone = payment.getPayerPhone();
        this.status = payment.getStatus() != null ? payment.getStatus().name() : null;
        this.providerRef = payment.getProviderRef();
        this.failureReason = payment.getFailureReason();
        this.notes = payment.getNotes();
        this.paymentReference = payment.getPaymentReference();
        this.receiptNumber = payment.getReceiptNumber();
        this.paymentUrl = payment.getPaymentUrl();
        this.controlNumber = payment.getControlNumber();
        this.requestedAt = payment.getCreatedAt();
        this.paidAt = payment.getPaidAt();
    }
}
