package ksc.go.tz.sitesAndAssests.controllers;

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
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SiteController {
    private final SiteService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new site")
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

    @Operation(summary = "update site ")
    @Permission(name="UPDATE SITE", code = "UPDATE_SITE")
    @PutMapping("/sites/{id}")
    public ApiResponseUtil.ApiResponseEntity<SiteResponseDto> updateSite(@PathVariable("id") String siteId, @RequestBody SiteDto siteDto, Authentication authentication) {
        SiteResponseDto updatedSite = siteService.updateSite(siteId, siteDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSite,"Site updated successful",ResponseEnum.SUCCESS);

    }
    @Operation(summary = "soft delete site ")
    @Permission(name="DELETE SITE", code = "DELETE_SITE")
    @DeleteMapping("/sites/{siteId}")
    public ApiResponseUtil.ApiResponseEntity<SiteResponseDto> deleteById(@PathVariable (name = "id") String siteId, Authentication authentication){
        SiteResponseDto sites =  siteService.deleteById(siteId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, sites,"Site deleted successful",ResponseEnum.SUCCESS);

    }

    // GET /api/v1/sites/{siteId}/contracts

    // GET /api/v1/sites/{siteId}/jobs

    // GET /api/v1/sites/{siteId}/quotes


}
