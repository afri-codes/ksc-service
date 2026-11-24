package ksc.go.tz.sitesAndAssests.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body for registering or updating a site")
public class SiteDto {

    @NotNull(message = "Site type must be provided")
    @Schema(description = "Type of site", example = "residential", requiredMode = Schema.RequiredMode.REQUIRED)
    private String siteType;

    @NotNull(message = "Area in square metres must be provided")
    @Schema(description = "Site area in square metres", example = "120.5", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal areaSqm;

    @NotNull(message = "Room count must be provided")
    @Schema(description = "Number of rooms", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer roomCount;

    @NotNull(message = "Address area must be provided")
    @Schema(description = "Address / area of the site", example = "Mikocheni, Dar es Salaam", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressArea;

    @Schema(description = "Ignored on input; coordinates are built from latitude and longitude")
    private String plotCoordinates;

    @NotNull(message = "Longitude must be provided")
    @Schema(description = "Longitude in decimal degrees", example = "39.2408", requiredMode = Schema.RequiredMode.REQUIRED)
    private String longitude;

    @NotNull(message = "Latitude must be provided")
    @Schema(description = "Latitude in decimal degrees", example = "-6.7618", requiredMode = Schema.RequiredMode.REQUIRED)
    private String latitude;

    @Schema(description = "Whether the site is secured/gated", example = "true")
    private Boolean secured;

    @Schema(description = "How staff access the site", example = "Gate pass")
    private String accessType;

    @NotBlank(message = "Service type must be provided")
    @Schema(description = "ID of the service requested for the site (Cleaning, Fumigation, Property Management). Must exist and be ACTIVE", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceId;

    @Schema(description = "ID of the selected cleaning depth. Required for Cleaning sites, optional for Fumigation and Property Management. Must exist and be ACTIVE", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String cleaningDepthId;

    @Schema(description = "IDs of the selected add-ons (optional, may be empty). Each must exist and be ACTIVE. On update this list replaces the existing add-ons", example = "[\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"]")
    private List<String> addOnIds = new ArrayList<>();
}
