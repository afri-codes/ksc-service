package ksc.go.tz.contractAndSubscriptions.entities;
import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.SignatureStatus;
import ksc.go.tz.quoting.entities.Quote;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Contract extends BaseEntity<UUID> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false, unique = true)
    private Quote quote;

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
    private SignatureStatus signatureStatus;

    @Column(name = "contract_value")
    private BigDecimal contractValue;

    @Column(name = "pdf_url", nullable = false)
    private String pdfUrl;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

}
