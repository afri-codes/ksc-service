package ksc.go.tz.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.job.entities.CrewMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Crew member")
public class CrewMemberResponseDto {

    private String memberId;
    private String crewId;
    private String crewName;
    private String staffId;
    private String staffName;
    private String memberRole;
    private LocalDate joinedOn;

    public CrewMemberResponseDto(CrewMember member) {
        this.memberId = member.getId().toString();
        this.crewId = member.getCrew().getId().toString();
        this.crewName = member.getCrew().getCrewName();
        this.staffId = member.getStaffId();
        this.staffName = member.getStaffName();
        this.memberRole = member.getMemberRole();
        this.joinedOn = member.getJoinedOn();
    }
}
