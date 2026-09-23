package ksc.go.tz.contractAndSubscriptions.dto;

import jakarta.validation.constraints.NotBlank;
import ksc.go.tz.enums.SubscriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter

public class SubscriptionDto {

    @NotBlank
    private String contractId;

    @NotBlank
    private SubscriptionStatus status;

    @NotBlank
    private String recurrence;

    @NotBlank
    private Integer cycleSize;

    @NotBlank
    private BigDecimal pricePerCycle;

    @NotBlank
    private LocalDate nextRunDate;
}
