package ksc.go.tz.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A priced line on a quotation or invoice")
public class LineItem {

    @Schema(description = "What is being charged for", example = "Cleaning depth: Deep")
    @Column(name = "description", nullable = false)
    private String description;

    @Schema(description = "Quantity", example = "1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Schema(description = "Unit price in TZS", example = "120000.00")
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Schema(description = "quantity × unitPrice, in TZS", example = "120000.00")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    public static LineItem of(String description, BigDecimal unitPrice) {
        return new LineItem(description, 1, unitPrice, unitPrice);
    }

    public LineItem copy() {
        return new LineItem(description, quantity, unitPrice, amount);
    }
}
