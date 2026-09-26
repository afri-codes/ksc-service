package ksc.go.tz.sitesAndAssests.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Site owner must be provided")
    private String siteType;

    @NotBlank(message = "Site type must be provided")
    private BigDecimal areaSqm;

    @NotBlank(message = "Room count must be provided")
    private Integer roomCount;

    @NotBlank(message = "Address area must be provided")
    private String addressArea;

    private String plotCoordinates;

    @NotBlank(message = "Longitude must be provided")
    private String longitude;

    @NotBlank(message = "Latitude must be provided")
    private String latitude;

    private Boolean secured;

    private String accessType;
}
