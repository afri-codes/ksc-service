package ksc.go.tz.billing.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Invoice extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "amount_due", precision = 19, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDate billingPeriodEnd;

    @Column
    private String status;

    @Column(name = "pdf_local_url")
    private String pdfLocalUrl;

    @Column(name = "pdf_url")
    private String pdfUrl;
}
