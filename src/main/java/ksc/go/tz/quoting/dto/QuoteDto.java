package ksc.go.tz.quoting.dto;

import jakarta.validation.constraints.NotNull;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuoteDto {

    @NotNull(message = "Site ID must be provided")
    private String siteId;

    @NotNull(message = "Service line must be provided")
    private LeadServiceType serviceLine;

    @NotNull(message = "Client tier must be provided")
    private String clientTier;

    @NotNull(message = "Hourly rate must be provided")
    private BigDecimal hourlyRate;

    @NotNull(message = "Estimated hours must be provided")
    private BigDecimal estimatedHours;

    @NotNull(message = "Price min must be provided")
    private BigDecimal priceMin;

    @NotNull(message = "Price max must be provided")
    private BigDecimal priceMax;

    @NotNull(message = "Valid until date must be provided")
    private LocalDate validUntil;

    @NotNull(message = "Frequency must be provided")
    private Frequency frequency;

    @NotNull(message = "Area in square meters must be provided")
    private BigDecimal areaSqm;

    private String status;
}
