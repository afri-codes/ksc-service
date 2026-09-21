package ksc.go.tz.job.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "checklist_items")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class ChecklistItem extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private JobChecklist checklist;

    @Column(name = "task_label", nullable = false)
    private String taskLabel;

    @Column(name = "is_complete")
    private Boolean complete;

    @Column(columnDefinition = "TEXT")
    private String notes;

}
