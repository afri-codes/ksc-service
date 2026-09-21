package ksc.go.tz.contractAndSubscriptions.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false)
    private String recurrence;

    @Column(name = "crew_size")
    private Integer crewSize;

    @Column
    private String frequency;

    @Column
    private String status;

    @Column(name = "next_run_date")
    private LocalDate nextRunDate;
}
