package ksc.go.tz.contractAndSubscriptions.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.SubscriptionStatus;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Subscription extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private String recurrence;  //enum

    private String shiftPreference;  //enum

    @Column(name = "crew_size")
    private Integer crewSize;

    @Column(name = "price_per_cycle")
    private BigDecimal pricePerCycle;

    @Column(name = "next_run_date")
    private LocalDate nextRunDate;
}
