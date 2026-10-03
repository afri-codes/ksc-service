package ksc.go.tz.contractAndSubscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.quotation.dto.QuoteResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Contract details")
public class ContractResponseDto {

    @Schema(description = "Contract ID")
    private String contractId;

    @Schema(description = "Contract number", example = "CT-20261002-7K3Q9A")
    private String contractNumber;

    @Schema(description = "DRAFT, SENT, SIGNED, ACTIVE, EXPIRED, TERMINATED or REJECTED", example = "DRAFT")
    private String status;

    @Schema(description = "PENDING, SIGNED or REJECTED", example = "PENDING")
    private String signatureStatus;

    private String quoteId;

    private String siteId;

    @Schema(description = "Quote the contract was created from (with its site)")
    private QuoteResponseDto quote;

    private String clientId;

    @Schema(example = "CLEANING")
    private String serviceLine;

    private LocalDate startDate;

    private LocalDate endDate;

    @Schema(example = "MONTHLY")
    private String frequency;

    @Schema(description = "Total contract value in TZS", example = "1680000.00")
    private BigDecimal contractValue;

    private String businessInfo;

    private String businessTin;

    private String businessBrelaNo;

    private String personalIdNo;

    private String terms;

    private String pdfUrl;

    private LocalDateTime sentAt;

    private LocalDateTime signedAt;

    private String signedBy;

    private LocalDateTime rejectedAt;

    private String rejectionReason;

    private LocalDateTime terminatedAt;

    private String terminationReason;

    private LocalDateTime createdAt;

    public ContractResponseDto(Contract contract) {
        this.contractId = contract.getId().toString();
        this.contractNumber = contract.getContractNumber();
        this.status = contract.getStatus() != null ? contract.getStatus().name() : null;
        this.signatureStatus = contract.getSignatureStatus() != null ? contract.getSignatureStatus().name() : null;
        this.quoteId = contract.getQuote().getId().toString();
        this.siteId = contract.getSite() != null ? contract.getSite().getId().toString() : null;
        this.quote = new QuoteResponseDto(contract.getQuote());
        this.clientId = contract.getClientId();
        this.serviceLine = contract.getServiceType() != null ? contract.getServiceType().name() : null;
        this.startDate = contract.getStartDate();
        this.endDate = contract.getEndDate();
        this.frequency = contract.getFrequency() != null ? contract.getFrequency().name() : null;
        this.contractValue = contract.getContractValue();
        this.businessInfo = contract.getBusinessInfo();
        this.businessTin = contract.getBusinessTin();
        this.businessBrelaNo = contract.getBusinessBrelaNo();
        this.personalIdNo = contract.getPersonalIdNo();
        this.terms = contract.getTerms();
        this.pdfUrl = contract.getPdfUrl();
        this.sentAt = contract.getSentAt();
        this.signedAt = contract.getSignedAt();
        this.signedBy = contract.getSignedBy();
        this.rejectedAt = contract.getRejectedAt();
        this.rejectionReason = contract.getRejectionReason();
        this.terminatedAt = contract.getTerminatedAt();
        this.terminationReason = contract.getTerminationReason();
        this.createdAt = contract.getCreatedAt();
    }
}
