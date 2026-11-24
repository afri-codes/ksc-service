package ksc.go.tz.masterData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.masterData.entities.CleaningDepth;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Cleaning depth details")
public class CleaningDepthResponseDto {

    @Schema(description = "Cleaning depth ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String cleaningDepthId;
    @Schema(description = "Cleaning depth name", example = "Deep")
    private String name;
    @Schema(description = "Price in TZS", example = "120000.00")
    private BigDecimal price;
    @Schema(description = "Description of the cleaning depth", example = "Thorough top-to-bottom cleaning including hard-to-reach areas")
    private String description;
    @Schema(description = "Record status", allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
    private String status;

    public CleaningDepthResponseDto(CleaningDepth cleaningDepth) {
        this.cleaningDepthId = cleaningDepth.getId().toString();
        this.name = cleaningDepth.getName();
        this.price = cleaningDepth.getPrice();
        this.description = cleaningDepth.getDescription();
        this.status = cleaningDepth.getStatus().name();
    }
}
