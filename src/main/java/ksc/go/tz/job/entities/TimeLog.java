package ksc.go.tz.job.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_logs")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class TimeLog extends BaseEntity<UUID> {


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "staff_id", nullable = false)
    private String staffId;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "geofence_zone")
    private String geofenceZone;
}
