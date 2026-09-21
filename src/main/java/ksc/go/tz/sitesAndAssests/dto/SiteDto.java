package ksc.go.tz.sitesAndAssests.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteDto {

    private String siteType;

    private BigDecimal areaSqm;

    private Integer roomCount;

    private String addressArea;

    private String plotCoordinates;

    private Boolean secured;

    private String accessType;
}
