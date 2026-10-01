package ksc.go.tz.sitesAndAssests.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteDto {

    @NotNull(message = "Site owner must be provided")
    private String siteType;

    @NotNull(message = "Site type must be provided")
    private BigDecimal areaSqm;

    @NotNull(message = "Room count must be provided")
    private Integer roomCount;

    @NotNull(message = "Address area must be provided")
    private String addressArea;

    private String plotCoordinates;

    @NotNull(message = "Longitude must be provided")
    private String longitude;

    @NotNull(message = "Latitude must be provided")
    private String latitude;

    private Boolean secured;

    private String accessType;

    @NotBlank(message = "Cleaning depth must be provided")
    private String cleaningDepthId;

    private List<String> addOnIds = new ArrayList<>();
}
