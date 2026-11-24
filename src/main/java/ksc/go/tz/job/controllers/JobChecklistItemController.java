package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import ksc.go.tz.job.dto.JobChecklistItemDto;
import ksc.go.tz.job.dto.JobChecklistItemResponseDto;
import ksc.go.tz.job.services.JobCheckListItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobChecklistItemController {
    private final JobCheckListItemService jobCheckListItemService;
    private final ApiResponseUtil apiResponseUtil;


  // POST /api/v1/checklists/{checklistId}/items
    @Operation(summary = "Create a new checklist item for a checklist")
    @Permission(name = "Create Checklist Item", code = "CREATE_CHECKLIST_ITEM")
    public ApiResponseUtil.ApiResponseEntity<JobChecklistItemResponseDto> createChecklistItemForChecklist(@RequestBody @Valid JobChecklistItemDto jobChecklistItemDto, Authentication authentication) {
        // Implementation for creating a new checklist item for a checklist
        return apiResponseUtil.getResponse(null, jobCheckListItemService.createChecklistItemForChecklist(jobChecklistItemDto, authentication), "Checklist item created successfully", null);
    }

    // GET /api/v1/checklists/{checklistId}/items
    @Operation(summary = "Get all checklist items for a checklist")
    @Permission(name = "View All Checklist Items", code = "VIEW_ALL_CHECKLIST_ITEMS")
    public ApiResponseUtil.ApiResponseEntity<List<JobChecklistItemResponseDto>> getAllChecklistItemsForChecklist(Authentication authentication) {
        // Implementation for getting all checklist items for a checklist
        return apiResponseUtil.getResponse(jobCheckListItemService.getAllChecklistItemsForChecklist(authentication));
    }

    // GET /api/v1/checklist-items/{id}
    @Operation(summary = "Get checklist item by ID")
    @Permission(name = "View Checklist Item By ID", code = "VIEW_CHECKLIST_ITEM_BY_ID")
    @GetMapping("/checklist-items/{checklistItemId}")
    public ApiResponseUtil.ApiResponseEntity<JobChecklistItemResponseDto> getChecklistItemById(@PathVariable ("checklistItemId") String checklistItemId, Authentication authentication) {
        // Implementation for getting a checklist item by ID
        return apiResponseUtil.getResponse(jobCheckListItemService.getChecklistItemById(checklistItemId, authentication));
    }

    // PUT /api/v1/checklist-items/{id}
    @Operation(summary = "Update checklist item by ID")
    @Permission(name = "Update Checklist Item By ID", code = "UPDATE_CHECKLIST_ITEM_BY_ID")
    @PutMapping("/checklist-items/{checklistItemId}")
    public ApiResponseUtil.ApiResponseEntity<JobChecklistItemResponseDto> updateChecklistItemById(@PathVariable ("checklistItemId") String checklistItemId, @RequestBody @Valid JobChecklistItemDto jobChecklistItemDto, Authentication authentication) {
        // Implementation for updating a checklist item by ID
        return apiResponseUtil.getResponse(null, jobCheckListItemService.updateChecklistItemById(checklistItemId, jobChecklistItemDto, authentication), "Checklist item updated successfully", null);
    }

    // PATCH /api/v1/checklist-items/{id}/complete

    // PATCH /api/v1/checklist-items/{id}/incomplete

    // DELETE /api/v1/checklist-items/{id}
    @Operation(summary = "Delete checklist item by ID")
    @Permission(name = "Delete Checklist Item By ID", code = "DELETE_CHECKLIST_ITEM_BY_ID")
    @DeleteMapping("/checklist-items/{checklistItemId}")
    public ApiResponseUtil.ApiResponseEntity<JobChecklistItemResponseDto> deleteChecklistItemById(@PathVariable ("checklistItemId") String checklistItemId, Authentication authentication) {
        // Implementation for deleting a checklist item by ID
        return apiResponseUtil.getResponse(null, jobCheckListItemService.deleteChecklistItemById(checklistItemId, authentication), "Checklist item deleted successfully", null);
    }


}
