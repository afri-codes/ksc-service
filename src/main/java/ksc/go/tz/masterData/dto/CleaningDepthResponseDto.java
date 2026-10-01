package ksc.go.tz.masterData.dto;

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
public class CleaningDepthResponseDto {

    private String cleaningDepthId;
    private String name;
    private BigDecimal price;
    private String description;
    private String status;

    public CleaningDepthResponseDto(CleaningDepth cleaningDepth) {
        this.cleaningDepthId = cleaningDepth.getId().toString();
        this.name = cleaningDepth.getName();
        this.price = cleaningDepth.getPrice();
        this.description = cleaningDepth.getDescription();
        this.status = cleaningDepth.getStatus().name();
    }
}
