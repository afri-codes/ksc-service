package ksc.go.tz.contractAndSubscriptions.dto;

import ksc.go.tz.contractAndSubscriptions.entities.Subscription;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter

public class SubscriptionResponseDto {

    private String contractId;

    private ContractResponseDto contract;

    private String status;

    private String recurrence;

    private Integer crewSize;

    private BigDecimal pricePerCycle;

    private LocalDate nextRunDate;

    public SubscriptionResponseDto(Subscription subscription){

        this.contractId = subscription.getContract().getId().toString();
        this.contract = new ContractResponseDto(subscription.getContract());
        this.status = subscription.getStatus().toString();
        this.recurrence = subscription.getRecurrence();
        this.crewSize = subscription.getCrewSize();
        this.pricePerCycle = subscription.getPricePerCycle();
        this.nextRunDate = subscription.getNextRunDate();
    }

}
