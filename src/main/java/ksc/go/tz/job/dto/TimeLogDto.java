package ksc.go.tz.job.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class TimeLogDto  {

    @NotNull(message = "Job ID must be provided")
    private String jobId;
    @NotNull(message = "Staff ID must be provided")
    private String staffId;

    @NotNull(message = "Location must be provided")
    private String geofenceZone;

}
