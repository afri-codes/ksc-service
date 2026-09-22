package ksc.go.tz.leads.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.leads.dto.LeadDto;
import ksc.go.tz.leads.dto.LeadsResponseDto;
import ksc.go.tz.leads.services.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LeadController {
    private final LeadService leadService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new lead")
    @Permission(name="SAVE NEW SITE", code = "SAVE_SITE")
    @PostMapping("/leads")
    public  ApiResponseUtil.ApiResponseEntity<LeadsResponseDto> saveLead(@RequestBody @Valid LeadDto leadDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, leadService.addLead(leadDto, createdBy), "Lead added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Get all leads with pagination, sorting and filtering by status, source and service line")
    @Permission(name = "VIEW ALL LEAD", code = "VIEW_LEAD")
    @GetMapping("/leads/pagination")
    public ApiResponseUtil.ApiResponseEntity<Page<LeadsResponseDto>> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String serviceLine,
            Authentication authentication
    ) {

        UUID userId = authDetailsExtractor.getUserId(authentication);
        Page<LeadsResponseDto> leads = leadService.getAllLeadsWithPaginationAndSortingAndFiltering(
                        page, size, sortBy, sortDir, status, source, serviceLine, userId);
        return apiResponseUtil.getResponse(leads);
    }

    @Operation(summary = "get all lead list ")
    @Permission(name="VIEW ALL LEAD", code = "VIEW_LEAD")
    @GetMapping("/leads")
    public ApiResponseUtil.ApiResponseEntity<List<LeadsResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(leadService.getAll(userId));

    }

    @Operation(summary = "update lead ")
    @Permission(name="UPDATE LEAD", code = "UPDATE_LEAD")
    @PutMapping("/leads/{id}")
    public ApiResponseUtil.ApiResponseEntity<LeadsResponseDto> updateLead(@PathVariable("id") String leadId, @RequestBody LeadDto leadDto, Authentication authentication) {
        LeadsResponseDto updatedLead = leadService.updateLead(leadId, leadDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedLead,"Lead updated successful",ResponseEnum.SUCCESS);

    }
    @Operation(summary = "soft delete lead ")
    @Permission(name="DELETE LEAD", code = "DELETE_LEAD")
    @DeleteMapping("/leads/{leadId}")
    public ApiResponseUtil.ApiResponseEntity<LeadsResponseDto> deleteById(@PathVariable (name = "id") String leadId, Authentication authentication){
        LeadsResponseDto leadsResponseDto =  leadService.deleteById(leadId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, leadsResponseDto,"Lead deleted successful",ResponseEnum.SUCCESS);

    }

    @Operation(summary = "change lead status ")
    @Permission(name="CHANGE LEAD STATUS", code = "CHANGE_LEAD_STATUS")
    @PutMapping("/leads/{leadId}/status")
    public ApiResponseUtil.ApiResponseEntity<LeadsResponseDto> changeLeadStatus(@PathVariable("leadId") String leadId, @RequestParam("status") String status, Authentication authentication) {
        LeadsResponseDto updatedLead = leadService.changeLeadStatus(leadId, status, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedLead,"Lead status updated successfully", ResponseEnum.SUCCESS);
    }
}
