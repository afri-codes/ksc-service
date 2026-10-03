package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ksc.go.tz.job.dto.CrewMemberDto;
import ksc.go.tz.job.dto.CrewMemberResponseDto;
import ksc.go.tz.job.services.CrewMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Crew members", description = "Which staff belong to which crew; jobs are assigned to crews")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CrewMemberController {
    private final CrewMemberService crewMemberService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    @Operation(summary = "Add a staff member to a crew", description = "A staff member can belong to several crews, but only once to each.")
    @Permission(name = "ADD CREW MEMBER", code = "ADD_CREW_MEMBER")
    @PostMapping("/crews/{crewId}/members")
    public ApiResponseUtil.ApiResponseEntity<CrewMemberResponseDto> addMember(@PathVariable("crewId") String crewId,
                                                                              @RequestBody @Valid CrewMemberDto dto,
                                                                              Authentication authentication) {
        CrewMemberResponseDto member = crewMemberService.addMember(crewId, dto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, member, "Crew member added", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "List a crew's members")
    @Permission(name = "VIEW CREW MEMBERS", code = "VIEW_CREW_MEMBERS")
    @GetMapping("/crews/{crewId}/members")
    public ApiResponseUtil.ApiResponseEntity<List<CrewMemberResponseDto>> getMembers(@PathVariable("crewId") String crewId) {
        return apiResponseUtil.getResponse(crewMemberService.getMembers(crewId));
    }

    @Operation(summary = "Crews a staff member belongs to")
    @Permission(name = "VIEW CREW MEMBERS", code = "VIEW_CREW_MEMBERS")
    @GetMapping("/staff/{staffId}/crews")
    public ApiResponseUtil.ApiResponseEntity<List<CrewMemberResponseDto>> getCrewsOfStaff(@PathVariable("staffId") String staffId) {
        return apiResponseUtil.getResponse(crewMemberService.getCrewsOfStaff(staffId));
    }

    @Operation(summary = "Remove a staff member from a crew", description = "Soft delete; the staff member can be added again later.")
    @Permission(name = "REMOVE CREW MEMBER", code = "REMOVE_CREW_MEMBER")
    @DeleteMapping("/crews/{crewId}/members/{staffId}")
    public ApiResponseUtil.ApiResponseEntity<CrewMemberResponseDto> removeMember(@PathVariable("crewId") String crewId,
                                                                                 @PathVariable("staffId") String staffId,
                                                                                 Authentication authentication) {
        CrewMemberResponseDto member = crewMemberService.removeMember(crewId, staffId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, member, "Crew member removed", ResponseEnum.SUCCESS);
    }
}
