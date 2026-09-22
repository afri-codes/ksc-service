package ksc.go.tz.quoting.dto;

import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.quoting.entities.Quote;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;
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
public class QuoteResponseDto {

    private String quoteId;

    private String siteId;

    private SiteResponseDto site;

    private LeadServiceType serviceLine;

    private String clientTier;

    private BigDecimal hourlyRate;

    private BigDecimal estimatedHours;

    private BigDecimal priceMin;

    private BigDecimal priceMax;

    private LocalDate validUntil;

    private String status;

    public QuoteResponseDto(Quote quote) {
        this.siteId = quote.getSite().getId().toString();
        this.quoteId = quote.getId().toString();
        this.site = new SiteResponseDto(quote.getSite());
        this.serviceLine = quote.getServiceType();
        this.clientTier = quote.getClientTier();
        this.hourlyRate = quote.getHourlyRate();
        this.estimatedHours = quote.getEstimatedHours();
        this.priceMin = quote.getPriceMin();
        this.priceMax = quote.getPriceMax();
        this.validUntil = quote.getValidUntil();
        this.status = quote.getStatus().toString();

    }
}
