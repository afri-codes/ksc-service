package ksc.go.tz.masterData.dto;

import jakarta.validation.constraints.DecimalMin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for creating or updating a service")
public class ServiceDto {

    @NotNull(message = "Service name is required")
    @Schema(description = "Name of the service", example = "Cleaning", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceName;

    @DecimalMin(value = "0.0", message = "Price must not be negative")
    @Schema(description = "Fixed price in TZS charged once per site for this service. Leave empty for no fixed charge (e.g. Cleaning, which is priced by cleaning depth)", example = "150000.00")
    private BigDecimal price;

}
