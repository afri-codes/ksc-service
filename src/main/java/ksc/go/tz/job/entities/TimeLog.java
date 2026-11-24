package ksc.go.tz.job.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.TimeLogStatus;
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

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "geofence_zone_in")
    private String geofenceZoneIn;

    @Column(name = "geofence_zone_out")
    private String geofenceZoneOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "clock_in_status", nullable = false)
    private TimeLogStatus clockInStatus;


    @Enumerated(EnumType.STRING)
    @Column(name = "clock_out_status", nullable = true)
    private TimeLogStatus clockOutStatus;
}
