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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api")
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

    @Operation(summary = "get all lead list ")
    @Permission(name="VIEW ALL LEAD", code = "VIEW_LEAD")
    @GetMapping("/leads")
    public ApiResponseUtil.ApiResponseEntity<List<LeadsResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(leadService.getAll(userId));

    }

    @Operation(summary = "update lead ")
    @Permission(name="UPDATE LEAD", code = "UPDATE_LEAD")
    @PutMapping("/lead/{id}")
    public ApiResponseUtil.ApiResponseEntity<LeadsResponseDto> updateLead(@PathVariable("id") String leadId, @RequestBody LeadDto leadDto, Authentication authentication) {
        LeadsResponseDto updatedLead = leadService.updateLead(leadId, leadDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedLead,"Lead updated successful",ResponseEnum.SUCCESS);

    }
    @Operation(summary = "soft delete lead ")
    @Permission(name="DELETE LEAD", code = "DELETE_LEAD")
    @DeleteMapping("/site/{leadId}")
    public ApiResponseUtil.ApiResponseEntity<LeadsResponseDto> deleteById(@PathVariable (name = "id") String leadId, Authentication authentication){
        LeadsResponseDto leadsResponseDto =  leadService.deleteById(leadId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, leadsResponseDto,"Lead deleted successful",ResponseEnum.SUCCESS);

    }
}
