package ksc.go.tz.job.dto;

import ksc.go.tz.job.entities.TimeLog;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class TimeLogResponseDto {

    private String timeLogId;

    private String jobId;

    private JobResponseDto job;

    private String staffId;

    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    private String geofenceZoneIn;

    private String geofenceZoneOut;

    public TimeLogResponseDto(TimeLog timeLog) {
        this.timeLogId = timeLog.getId().toString();
        this.job = new JobResponseDto(timeLog.getJob());
        this.staffId = timeLog.getStaffId().toString();
        this.clockIn = timeLog.getClockIn();
        this.clockOut = timeLog.getClockOut();
        this.geofenceZoneIn = timeLog.getGeofenceZoneIn();
        this.geofenceZoneOut = timeLog.getGeofenceZoneOut();
    }
}
