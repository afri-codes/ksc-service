package ksc.go.tz.quotation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuoteResponseDto {

    private String quoteId;

    private String quoteNumber;

    private String siteId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SiteResponseDto site;

    private LeadServiceType serviceLine;

    private String clientTier;

    private BigDecimal hourlyRate;

    private BigDecimal estimatedHours;

    private BigDecimal priceMin;

    private BigDecimal priceMax;

    private LocalDate validUntil;

    private String status;

    private Frequency frequency;

    private BigDecimal areaSqm;

    private List<LineItem> items;


    public QuoteResponseDto(Quote quote) {
        this(quote, true);
    }

    /**
     * @param includeSite false when the quote is shown inside its own site's response, so the site is not repeated
     */
    public QuoteResponseDto(Quote quote, boolean includeSite) {
        this.siteId = quote.getSite().getId().toString();
        this.quoteId = quote.getId().toString();
        this.quoteNumber = quote.getQuoteNumber();
        this.site = includeSite ? new SiteResponseDto(quote.getSite()) : null;
        this.serviceLine = quote.getServiceType();
        this.clientTier = quote.getClientTier();
        this.hourlyRate = quote.getHourlyRate();
        this.estimatedHours = quote.getEstimatedHours();
        this.priceMin = quote.getPriceMin();
        this.priceMax = quote.getPriceMax();
        this.validUntil = quote.getValidUntil();
        this.status = quote.getStatus().toString();
        this.frequency = quote.getFrequency();
        this.areaSqm = quote.getAreaSqm();
        this.items = List.copyOf(quote.getItems());

    }
}
