package ksc.go.tz.quoting.dto;

import ksc.go.tz.enums.ServiceLine;
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

    private String siteId;

    private ServiceLine serviceLine;

    private String clientTier;

    private BigDecimal hourlyRate;

    private BigDecimal estimatedHours;

    private BigDecimal priceMin;

    private BigDecimal priceMax;

    private LocalDate validUntil;

    private String status;
}
