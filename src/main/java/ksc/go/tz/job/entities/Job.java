package ksc.go.tz.job.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.entities.Subscription;
import ksc.go.tz.enums.JobStatus;
import ksc.go.tz.enums.ServiceLine;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Job extends BaseEntity<UUID> {


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Sites site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id")
    private Crew crew;

    @Column(name = "service_line")
    private String serviceLine;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "scheduled_start")
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end")
    private LocalDateTime scheduledEnd;

    @Enumerated(EnumType.STRING)
    private JobStatus status;
}
