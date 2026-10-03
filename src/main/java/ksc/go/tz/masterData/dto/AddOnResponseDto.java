package ksc.go.tz.masterData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.masterData.entities.AddOn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Add-on details")
public class AddOnResponseDto {

    @Schema(description = "Add-on ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String addOnId;
    @Schema(description = "Add-on name", example = "Window Cleaning")
    private String name;
    @Schema(description = "Price in TZS", example = "20000.00")
    private BigDecimal price;
    @Schema(description = "Description of the add-on", example = "Interior and exterior window and glass cleaning")
    private String description;
    @Schema(description = "Record status", allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
    private String status;

    public AddOnResponseDto(AddOn addOn) {
        this.addOnId = addOn.getId().toString();
        this.name = addOn.getName();
        this.price = addOn.getPrice();
        this.description = addOn.getDescription();
        this.status = addOn.getStatus().name();
    }
}
