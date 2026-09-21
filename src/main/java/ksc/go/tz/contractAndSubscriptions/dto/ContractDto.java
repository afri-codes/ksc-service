package ksc.go.tz.contractAndSubscriptions.dto;

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

    private String businessHours;

    private String businessDays;

    private String frequency;

    private BigDecimal contractValue;

    private String staffUrl;

    private String signatureStatus;

    private LocalDate nextDate;
}
