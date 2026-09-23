package ksc.go.tz.job.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class TimeLogDto  {

    private String jobId;

    private String staffId;

    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    private String geofenceZone;
}
