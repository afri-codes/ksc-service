package ksc.go.tz.contractAndSubscriptions.dto;

import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.quoting.dto.QuoteResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponseDto {

    private String quoteId;

    private String contractId;

    private QuoteResponseDto quote;

    private String userId;

    private String serviceLine;

    private LocalDate startDate;

    private LocalDate endDate;

    private String businessHours;

    private String businessDays;

    private String frequency;

    private BigDecimal contractValue;

    private String staffUrl;

    private String signatureStatus;

    private LocalDate nextDate;

    public ContractResponseDto(Contract contract) {
        this.quoteId = contract.getQuote().getId().toString();
        this.contractId = contract.getId().toString();
        this.quote = new QuoteResponseDto(contract.getQuote());
        this.userId = contract.getUserId();
        this.serviceLine = contract.getServiceLine();
        this.startDate = contract.getStartDate();
        this.endDate = contract.getEndDate();
        this.businessHours = contract.getBusinessHours();
        this.businessDays = contract.getBusinessDays();
        this.frequency = contract.getFrequency();
        this.contractValue = contract.getContractValue();
        this.staffUrl = contract.getStaffUrl();
        this.signatureStatus = contract.getSignatureStatus();
        this.nextDate = contract.getNextDate();


    }
}
