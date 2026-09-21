package ksc.go.tz.contractAndSubscriptions.entities;
import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    private  String userId;

    @Column(name = "service_line")
    private String serviceLine;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "business_hours")
    private String businessHours;

    @Column(name = "business_days")
    private String businessDays;

    private String frequency;

    @Column(name = "contract_value", precision = 19, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "staff_url")
    private String staffUrl;

    @Column(name = "signature_status")
    private String signatureStatus;

    @Column(name = "next_date")
    private LocalDate nextDate;

}
