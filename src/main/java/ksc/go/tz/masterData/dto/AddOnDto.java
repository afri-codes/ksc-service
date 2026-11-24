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
@Schema(description = "Request body for creating or updating an add-on")
public class AddOnDto {

    @NotBlank(message = "Add-on name is required")
    @Schema(description = "Unique add-on name (case-insensitive)", example = "Window Cleaning", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must not be negative")
    @Schema(description = "Price in TZS. Must not be negative", example = "20000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "Optional description of the add-on", example = "Interior and exterior window and glass cleaning")
    private String description;

}
