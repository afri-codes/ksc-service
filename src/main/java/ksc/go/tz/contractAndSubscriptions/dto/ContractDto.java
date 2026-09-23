package ksc.go.tz.contractAndSubscriptions.dto;

import jakarta.persistence.*;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.SignatureStatus;
import ksc.go.tz.quoting.entities.Quote;
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
public class ContractDto {

    private String quoteId;

    private String userId;

    private String serviceLine;

    private LocalDate startDate;

    private LocalDate endDate;

    private String frequency;

    private BigDecimal contractValue;

    private String signatureStatus;

    private LocalDate nextDate;

    private String  clientId;

    private String businessInfo;

    private String personalIdNo;

    private LocalDate paidAt;


}
