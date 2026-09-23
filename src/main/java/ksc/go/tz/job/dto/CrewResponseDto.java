package ksc.go.tz.job.dto;

import ksc.go.tz.job.entities.Crew;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CrewResponseDto {

    private String crewName;

    private String crewId;

    private String supervisorId;

    private String supervisorName;

    private String zone;

    public CrewResponseDto(Crew crew){
        this.crewId = crew.getId().toString();
        this.crewName = crew.getCrewName();
        this.supervisorId = crew.getSupervisorId();
        this.supervisorName = crew.getSupervisorName();
        this.zone = crew.getZone();
    }

}
