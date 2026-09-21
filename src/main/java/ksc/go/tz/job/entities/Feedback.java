package ksc.go.tz.job.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "feedbacks")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Feedback extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "submitted_by", nullable = false)
    private String submittedBy;

    @Column(name = "service_rating")
    private Integer serviceRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "dispute_status")
    private String disputeStatus;

}
