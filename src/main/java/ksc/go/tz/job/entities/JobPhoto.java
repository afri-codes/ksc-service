package ksc.go.tz.job.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_photos")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class JobPhoto extends BaseEntity<UUID> {


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column
    private String stage;

    @Column(name = "storage_url")
    private String storageUrl;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column
    private Boolean synced;
}
