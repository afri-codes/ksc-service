package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.job.dto.CrewDto;
import ksc.go.tz.job.dto.CrewResponseDto;
import ksc.go.tz.job.services.CrewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobCrewController {
    private final CrewService crewService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   //  POST /api/v1/crews
    @Operation(summary = "Create a new crew")
    @Permission(name = "Create Crew", code = "CREATE_CREW")
    public ApiResponseUtil.ApiResponseEntity<CrewResponseDto> createCrew(@RequestBody @Valid CrewDto crewDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null,  crewService.createCrew(crewDto, createdBy), "Crew created successfully", null);
    }

    // GET /api/v1/crews
    @Operation(summary = "Get all crews")
    @Permission(name = "View All Crews", code = "VIEW_ALL_CREWS")
    public ApiResponseUtil.ApiResponseEntity<List<CrewResponseDto>> getAllCrews(Authentication authentication) {
        return apiResponseUtil.getResponse(crewService.getAllCrews(authentication));
    }

    // GET /api/v1/crews/{id}
    @Operation(summary = "Get crew by ID")
    @Permission(name = "View Crew By ID", code = "VIEW_CREW_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<CrewResponseDto> getCrewById(UUID crewId, Authentication authentication) {
        return apiResponseUtil.getResponse(crewService.getCrewById(crewId, authentication));
    }

    //PUT /api/v1/crews/{id}
    @Operation(summary = "Update crew by ID")
    @Permission(name = "Update Crew By ID", code = "UPDATE_CREW_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<CrewResponseDto> updateCrewById(UUID crewId, @RequestBody @Valid CrewDto crewDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, crewService.updateCrewById(crewId, crewDto, authentication), "Crew updated successfully", null);
    }

    // DELETE /api/v1/crews/{id}
    @Operation(summary = "Delete crew by ID")
    @Permission(name = "Delete Crew By ID", code = "DELETE_CREW_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<CrewResponseDto> deleteCrewById(UUID crewId, Authentication authentication) {
        return apiResponseUtil.getResponse(null, crewService.deleteCrewById(crewId, authentication), "Crew deleted successfully", null);
    }

    // POST /api/v1/crews/{id}/supervisor
    @Operation(summary = "Assign a supervisor to a crew")
    @Permission(name = "Assign Supervisor to Crew", code = "ASSIGN_SUPERVISOR_TO_CREW")
    public ApiResponseUtil.ApiResponseEntity<CrewResponseDto> assignSupervisorToCrew(UUID crewId, UUID supervisorId, Authentication authentication) {
        return apiResponseUtil.getResponse(null, crewService.assignSupervisorToCrew(crewId, supervisorId, authentication), "Supervisor assigned to crew successfully", null);
    }

    // GET /api/v1/crews/{id}/members

    // POST /api/v1/crews/{id}/members

    // DELETE /api/v1/crews/{id}/members/{staffId}

    // GET /api/v1/crews/{id}/jobs

}
