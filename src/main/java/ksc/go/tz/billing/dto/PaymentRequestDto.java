package ksc.go.tz.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import ksc.go.tz.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to collect a payment for an invoice through the payment service (MOBILE_MONEY only)")
public class PaymentRequestDto {

    @NotNull(message = "Payment method must be provided")
    @Schema(description = "How the client will pay: MOBILE_MONEY (PIN prompt), CARD or CHECKOUT (payment link), BILLPAY or BANK (control number). CASH uses the cash endpoint; PAYPAL, STRIPE and OTHER are not supported", example = "MOBILE_MONEY", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentMethod method;

    @Schema(description = "Client phone number (required for every method by the payment service; for MOBILE_MONEY it is the phone that gets the PIN prompt)", example = "0712345678")
    private String payerPhone;

    @Schema(description = "Payer's name. Defaults to 'KSC invoice <number>'", example = "Jane Doe")
    private String payerName;

    @Email(message = "Payer email must be valid")
    @Schema(description = "Payer's email. Required only when the payment service provider is SELCOM", example = "jane@example.com")
    private String payerEmail;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount to collect in TZS. Defaults to the invoice's remaining balance; cannot exceed it", example = "140000.00")
    private BigDecimal amount;
}
