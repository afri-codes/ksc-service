package ksc.go.tz.sitesAndAssests.dto;

import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteResponseDto {

    private String siteId;

    private String siteType;

    private BigDecimal areaSqm;

    private Integer roomCount;

    private String addressArea;

    private String plotCoordinates;

    private String longitude;

    private String latitude;

    private Boolean secured;

    private String accessType;

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
    }
}
