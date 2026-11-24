package ksc.go.tz.masterData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request body for creating or updating a cleaning depth")
public class CleaningDepthDto {

    @NotBlank(message = "Cleaning depth name is required")
    @Schema(description = "Unique cleaning depth name (case-insensitive)", example = "Deep", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must not be negative")
    @Schema(description = "Price in TZS. Must not be negative", example = "120000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "Optional description of the cleaning depth", example = "Thorough top-to-bottom cleaning including hard-to-reach areas")
    private String description;

}
