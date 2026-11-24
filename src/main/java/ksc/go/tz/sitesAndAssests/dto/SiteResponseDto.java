package ksc.go.tz.sitesAndAssests.dto;

import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.quotation.dto.QuoteResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import ksc.go.tz.masterData.dto.ServiceResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.masterData.dto.AddOnResponseDto;
import ksc.go.tz.masterData.dto.CleaningDepthResponseDto;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Site details including selected cleaning depth, add-ons and total price")
public class SiteResponseDto {

    @Schema(description = "Site ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String siteId;

    @Schema(description = "Type of site", example = "residential")
    private String siteType;

    @Schema(description = "Site area in square metres", example = "120.5")
    private BigDecimal areaSqm;

    @Schema(description = "Number of rooms", example = "4")
    private Integer roomCount;

    @Schema(description = "Address / area of the site", example = "Mikocheni, Dar es Salaam")
    private String addressArea;

    @Schema(description = "Plot coordinates", example = "{latitude=-6.7618, longitude=39.2408}")
    private String plotCoordinates;

    @Schema(description = "Longitude in decimal degrees", example = "39.2408")
    private String longitude;

    @Schema(description = "Latitude in decimal degrees", example = "-6.7618")
    private String latitude;

    @Schema(description = "Whether the site is secured/gated", example = "true")
    private Boolean secured;

    @Schema(description = "How staff access the site", example = "Gate pass")
    private String accessType;

    @Schema(description = "Service requested for the site")
    private ServiceResponseDto service;

    @Schema(description = "Selected cleaning depth")
    private CleaningDepthResponseDto cleaningDepth;

    @Schema(description = "Selected add-ons")
    private List<AddOnResponseDto> addOns;

    @Schema(description = "Calculated by the server: cleaning depth price + sum of add-on prices (TZS). Recalculated on every create/update", example = "140000.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalPrice;

    @Schema(description = "The site's current quotation (latest not expired or rejected); null if it has none", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String quoteId;

    @Schema(description = "Number of the site's current quotation; null if it has none", example = "QT-20261002-7K3Q9A")
    private String quoteNumber;

    @Schema(description = "The site's current invoice (latest not cancelled); null if it has none", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String invoiceId;

    @Schema(description = "Number of the site's current invoice; null if it has none", example = "INV-20261002-7K3Q9A")
    private String invoiceNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Full current quotation with line items (GET /sites/{id} only; omitted from the list and when the site has none)")
    private QuoteResponseDto quote;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Full current invoice with line items, amount paid and balance due (GET /sites/{id} only; omitted from the list and when the site has none)")
    private InvoiceResponseDto invoice;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "All payments for this site's invoices (including superseded ones), newest first; empty when none (GET /sites/{id} only)")
    private List<PaymentResponseDto> payments;

    public SiteResponseDto(Sites sites) {
        this.siteId = sites.getId().toString();
        this.siteType = sites.getSiteType();
        this.areaSqm = sites.getAreaSqm();
        this.longitude = sites.getLongitude();
        this.latitude = sites.getLatitude();
        this.roomCount = sites.getRoomCount();
        this.addressArea = sites.getAddressArea();
        this.plotCoordinates = sites.getPlotCoordinates().toString();
        this.secured = sites.getSecured();
        this.accessType = sites.getAccessType();
        this.service = sites.getService() != null ? new ServiceResponseDto(sites.getService()) : null;
        this.cleaningDepth = sites.getCleaningDepth() != null ? new CleaningDepthResponseDto(sites.getCleaningDepth()) : null;
        this.addOns = sites.getAddOns().stream().map(AddOnResponseDto::new).toList();
        this.totalPrice = sites.getTotalPrice();
    }
}
