package ksc.go.tz.contractAndSubscriptions.entities;
import ksc.go.tz.enums.ContractStatus;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.SignatureStatus;
import ksc.go.tz.quotation.entities.Quote;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"quote", "site"})
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Contract extends BaseEntity<UUID> {

    @Column(name = "contract_number", unique = true)
    private String contractNumber;

    // One live contract per quote is enforced in ContractServiceImpl (not a DB unique key), so a
    // deleted DRAFT contract's quote can get a new contract.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Sites site;

    @Column(name = "client_id", nullable = false)
    private String  clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private LeadServiceType serviceType;

    @Column(name = "business_info")
    private String businessInfo;

    @Column(name = "business_tin")
    private String businessTin;

    @Column(name = "business_brela_no")
    private String businessBrelaNo;

    @Column(name = "personal_id_no")
    private String personalIdNo;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContractStatus status;

    @Enumerated(EnumType.STRING)
    private SignatureStatus signatureStatus;

    @Column(name = "contract_value", precision = 19, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "terms", length = 4000)
    private String terms;

    // Generated on request (GET /contracts/{id}/pdf); kept for an externally stored copy.
    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "signed_by")
    private String signedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "termination_reason")
    private String terminationReason;

}
