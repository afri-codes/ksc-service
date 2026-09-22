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

    private String clientId;

    private String serviceLine;

    private LocalDate startDate;

    private LocalDate endDate;

    private String businessDays;

    private String frequency;

    private BigDecimal contractValue;

    private String signatureStatus;


    public ContractResponseDto(Contract contract) {
        this.quoteId = contract.getQuote().getId().toString();
        this.contractId = contract.getId().toString();
        this.quote = new QuoteResponseDto(contract.getQuote());
        this.clientId = contract.getClientId();
        this.serviceLine = contract.getServiceType().toString();
        this.startDate = contract.getStartDate();
        this.endDate = contract.getEndDate();
        this.frequency = contract.getFrequency().toString();
        this.contractValue = contract.getContractValue();
        this.signatureStatus = contract.getSignatureStatus().toString();

    }
}
