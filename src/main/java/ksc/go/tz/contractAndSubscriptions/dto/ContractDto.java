package ksc.go.tz.contractAndSubscriptions.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Start date is required")
    private LocalDate startDate;

    @NotBlank(message = "End date is required")
    private LocalDate endDate;

    private String frequency;

    @NotBlank(message = "Contract value is required")
    private BigDecimal contractValue;

    private String signatureStatus;

    @NotBlank(message = "Business TIN is required")
    private String businessTin;

    @NotBlank(message = "Business Brela No is required")
    private String businessBrelaNo;

    @NotBlank(message = "Next Date is required")
    private LocalDate nextDate;

    private String  clientId;

    @NotBlank(message = "Business Info is required")
    private String businessInfo;

    @NotBlank(message = "PDF URL is required")
    private String pdfUrl;

    @NotBlank(message = "Personal ID No is required")
    private String personalIdNo;

    private LocalDate paidAt;


}
