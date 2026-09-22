package ksc.go.tz.job.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "crews")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Crew extends BaseEntity<UUID> {

    @Column(name = "crew_name", nullable = false)
    private String crewName;

    @Column(name = "supervisorId")
    private String supervisor;

    @Column(name = "decision_score")
    private String decisionScore;

}
