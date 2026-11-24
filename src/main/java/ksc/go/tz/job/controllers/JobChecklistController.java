package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import ksc.go.tz.job.dto.JobCheckListDto;
import ksc.go.tz.job.dto.JobCheckListResponseDto;
import ksc.go.tz.job.services.JobCheckListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobChecklistController {
    private final JobCheckListService  jobCheckListService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/jobs/{jobId}/checklists
    @Operation(summary = "Create a new checklist for a job")
    @Permission(name = "Create Checklist", code = "CREATE_CHECKLIST")
    @PostMapping("/jobs/{jobId}/checklists")
    public ApiResponseUtil.ApiResponseEntity<JobCheckListResponseDto> createChecklistForJob(@RequestBody @Valid JobCheckListDto jobCheckListDto, Authentication authentication) {
        // Implementation for creating a new checklist for a job
        return apiResponseUtil.getResponse(null, jobCheckListService.createChecklistForJob(jobCheckListDto, authentication) , "Checklist created successfully", null);
    }

    // GET /api/v1/jobs/{jobId}/checklists
    @Operation(summary = "Get all checklists for a job")
    @Permission(name = "View All Checklists", code = "VIEW_ALL_CHECKLISTS")
    public ApiResponseUtil.ApiResponseEntity<List<JobCheckListResponseDto>> getAllChecklistsForJob(Authentication authentication) {
        // Implementation for getting all checklists for a job
        return apiResponseUtil.getResponse(jobCheckListService.getAllChecklistsForJob(authentication));
    }

    // GET /api/v1/checklists/{id}
    @Operation(summary = "Get checklist by ID")
    @Permission(name = "View Checklist By ID", code = "VIEW_CHECKLIST_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<JobCheckListResponseDto> getChecklistById(String checklistId, Authentication authentication) {
        // Implementation for getting a checklist by ID
        return apiResponseUtil.getResponse(jobCheckListService.getChecklistById(checklistId, authentication));
    }

    // PUT /api/v1/checklists/{id}
    @Operation(summary = "Update checklist by ID")
    @Permission(name = "Update Checklist By ID", code = "UPDATE_CHECKLIST_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<JobCheckListResponseDto> updateChecklistById(String checklistId, @RequestBody @Valid JobCheckListDto jobCheckListDto, Authentication authentication) {
        // Implementation for updating a checklist by ID
        return apiResponseUtil.getResponse(null, jobCheckListService.updateChecklistById(checklistId, jobCheckListDto, authentication), "Checklist updated successfully", null);
    }

    // DELETE /api/v1/checklists/{id}
    @Operation(summary = "Delete checklist by ID")
    @Permission(name = "Delete Checklist By ID", code = "DELETE_CHECKLIST_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<JobCheckListResponseDto> deleteChecklistById(String checklistId, Authentication authentication) {
        // Implementation for deleting a checklist by ID
        return apiResponseUtil.getResponse(null, jobCheckListService.deleteChecklistById(checklistId, authentication), "Checklist deleted successfully", null);
    }

}
