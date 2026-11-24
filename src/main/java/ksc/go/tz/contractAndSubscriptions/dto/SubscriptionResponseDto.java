package ksc.go.tz.contractAndSubscriptions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private String subscriptionId;

    private String contractId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ContractResponseDto contract;

    private String status;

    private String recurrence;

    private Integer crewSize;

    private BigDecimal pricePerCycle;

    private LocalDate nextRunDate;

    public SubscriptionResponseDto(Subscription subscription){
        this(subscription, true);
    }

    /**
     * @param includeContract false when listed under its own contract, so the contract is not repeated
     */
    public SubscriptionResponseDto(Subscription subscription, boolean includeContract) {
        this.subscriptionId = subscription.getId() != null ? subscription.getId().toString() : null;
        this.contractId = subscription.getContract().getId().toString();
        this.contract = includeContract ? new ContractResponseDto(subscription.getContract()) : null;
        this.status = subscription.getStatus() != null ? subscription.getStatus().toString() : null;
        this.recurrence = subscription.getRecurrence();
        this.crewSize = subscription.getCrewSize();
        this.pricePerCycle = subscription.getPricePerCycle();
        this.nextRunDate = subscription.getNextRunDate();
    }

}
