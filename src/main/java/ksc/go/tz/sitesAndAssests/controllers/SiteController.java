package ksc.go.tz.sitesAndAssests.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.sitesAndAssests.dto.SiteDto;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import ksc.go.tz.sitesAndAssests.services.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Sites", description = "Register and manage client sites")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SiteController {
    private final SiteService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new site", description = "Registers a site. A cleaning depth (cleaningDepthId) is required and any number of add-ons (addOnIds) may be selected; all must be ACTIVE. totalPrice is calculated by the server as the cleaning depth price plus the sum of add-on prices.")
    @Permission(name="SAVE NEW SITE", code = "SAVE_SITE")
    @PostMapping("/sites")
    public  ApiResponseUtil.ApiResponseEntity<SiteResponseDto> saveSite(@RequestBody @Valid SiteDto siteDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, siteService.addSite(siteDto, createdBy), "Site added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get all site list ")
    @Permission(name="VIEW ALL SITE", code = "VIEW_SITE")
    @GetMapping("/sites")
    public ApiResponseUtil.ApiResponseEntity<List<SiteResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(siteService.getAll(userId));

    }

    @Operation(summary = "get site by id ")
    @Permission(name="VIEW SITE BY ID", code = "VIEW_SITE_BY_ID")
    @GetMapping("/sites/{id}")
    public ApiResponseUtil.ApiResponseEntity<SiteResponseDto> getById(@PathVariable("id") String siteId){
        Optional<SiteResponseDto> site = siteService.getById(siteId);
        if (site.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Site not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(site.get());
    }

    @Operation(summary = "update site ", description = "Updates a site. cleaningDepthId is required; addOnIds replaces the existing add-ons. totalPrice is recalculated from current prices. If the pricing changes, the open quotation is EXPIRED, the pending invoice CANCELLED, and a new quotation and invoice are generated (their IDs are returned). Pricing changes are rejected once the quotation is ACCEPTED or an invoice is no longer PENDING.")
    @Permission(name="UPDATE SITE", code = "UPDATE_SITE")
    @PutMapping("/sites/{id}")
    public ApiResponseUtil.ApiResponseEntity<SiteResponseDto> updateSite(@PathVariable("id") String siteId, @RequestBody @Valid SiteDto siteDto, Authentication authentication) {
        SiteResponseDto updatedSite = siteService.updateSite(siteId, siteDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSite,"Site updated successful",ResponseEnum.SUCCESS);

    }
    @Operation(summary = "soft delete site ")
    @Permission(name="DELETE SITE", code = "DELETE_SITE")
    @DeleteMapping("/sites/{id}")
    public ApiResponseUtil.ApiResponseEntity<SiteResponseDto> deleteById(@PathVariable("id") String siteId, Authentication authentication){
        SiteResponseDto sites =  siteService.deleteById(siteId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, sites,"Site deleted successful",ResponseEnum.SUCCESS);

    }

    // GET /api/v1/sites/{siteId}/contracts

    // GET /api/v1/sites/{siteId}/jobs

    // GET /api/v1/sites/{siteId}/quotes


}
