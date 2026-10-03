package ksc.go.tz.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Cash received for an invoice, recorded by staff")
public class CashPaymentDto {

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Cash received in TZS. Defaults to the invoice's remaining balance; cannot exceed it", example = "140000.00")
    private BigDecimal amount;

    @NotBlank(message = "Receipt number must be provided")
    @Size(max = 100, message = "Receipt number must be at most 100 characters")
    @Schema(description = "Number of the paper/cash receipt given to the client", example = "RCPT-004512", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiptNumber;

    @PastOrPresent(message = "Received time cannot be in the future")
    @Schema(description = "When the cash was received. Defaults to now", example = "2026-10-02T09:30:00")
    private LocalDateTime receivedAt;

    @Size(max = 255, message = "Notes must be at most 255 characters")
    @Schema(description = "Optional note, e.g. who handed over the cash", example = "Paid to site supervisor")
    private String notes;
}
