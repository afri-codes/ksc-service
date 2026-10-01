package ksc.go.tz.masterData.dto;

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
public class AddOnResponseDto {

    private String addOnId;
    private String name;
    private BigDecimal price;
    private String description;
    private String status;

    public AddOnResponseDto(AddOn addOn) {
        this.addOnId = addOn.getId().toString();
        this.name = addOn.getName();
        this.price = addOn.getPrice();
        this.description = addOn.getDescription();
        this.status = addOn.getStatus().name();
    }
}
