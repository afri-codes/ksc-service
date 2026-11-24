package ksc.go.tz.masterData.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.masterData.dto.CleaningDepthDto;
import ksc.go.tz.masterData.dto.CleaningDepthResponseDto;
import ksc.go.tz.masterData.services.CleaningDepthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Cleaning Depths", description = "Master data: cleaning depth levels, each with its own price")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CleaningDepthController {
    private final CleaningDepthService cleaningDepthService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new cleaning depth", description = "Creates a cleaning depth. Name must be unique (case-insensitive) and price must not be negative. New cleaning depths are ACTIVE.")
    @Permission(name="SAVE NEW CLEANING DEPTH", code = "SAVE_CLEANING_DEPTH")
    @PostMapping("/cleaning-depths")
    public ApiResponseUtil.ApiResponseEntity<CleaningDepthResponseDto> saveCleaningDepth(@RequestBody @Valid CleaningDepthDto cleaningDepthDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, cleaningDepthService.addCleaningDepth(cleaningDepthDto, createdBy), "Cleaning depth added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get all cleaning depth list ")
    @Permission(name="VIEW ALL CLEANING DEPTH", code = "VIEW_CLEANING_DEPTH")
    @GetMapping("/cleaning-depths")
    public ApiResponseUtil.ApiResponseEntity<List<CleaningDepthResponseDto>> getAll(Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(cleaningDepthService.getAll(userId));
    }

    @Operation(summary = "get cleaning depth by id ")
    @Permission(name="VIEW CLEANING DEPTH BY ID", code = "VIEW_CLEANING_DEPTH_BY_ID")
    @GetMapping("/cleaning-depths/{id}")
    public ApiResponseUtil.ApiResponseEntity<CleaningDepthResponseDto> getById(@PathVariable("id") String cleaningDepthId) {
        Optional<CleaningDepthResponseDto> cleaningDepth = cleaningDepthService.getById(cleaningDepthId);
        if (cleaningDepth.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Cleaning depth not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(cleaningDepth.get());
    }

    @Operation(summary = "update cleaning depth ", description = "Updates name, price and description. Existing sites keep their stored total price until they are updated.")
    @Permission(name="UPDATE CLEANING DEPTH", code = "UPDATE_CLEANING_DEPTH")
    @PutMapping("/cleaning-depths/{id}")
    public ApiResponseUtil.ApiResponseEntity<CleaningDepthResponseDto> updateCleaningDepth(@PathVariable("id") String cleaningDepthId, @RequestBody @Valid CleaningDepthDto cleaningDepthDto, Authentication authentication) {
        CleaningDepthResponseDto updated = cleaningDepthService.updateCleaningDepth(cleaningDepthId, cleaningDepthDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updated, "Cleaning depth updated successful", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "soft delete cleaning depth ")
    @Permission(name="DELETE CLEANING DEPTH", code = "DELETE_CLEANING_DEPTH")
    @DeleteMapping("/cleaning-depths/{id}")
    public ApiResponseUtil.ApiResponseEntity<CleaningDepthResponseDto> deleteById(@PathVariable("id") String cleaningDepthId, Authentication authentication) {
        CleaningDepthResponseDto deleted = cleaningDepthService.deleteById(cleaningDepthId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, deleted, "Cleaning depth deleted successful", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "change cleaning depth status ", description = "Set status to ACTIVE or INACTIVE via the 'status' query parameter (case-insensitive). INACTIVE cleaning depths cannot be selected for sites.")
    @Permission(name="CHANGE CLEANING DEPTH STATUS", code = "CHANGE_CLEANING_DEPTH_STATUS")
    @PatchMapping("/cleaning-depths/{id}/status")
    public ApiResponseUtil.ApiResponseEntity<CleaningDepthResponseDto> changeStatus(@PathVariable("id") String cleaningDepthId, @RequestParam("status") String status, Authentication authentication) {
        CleaningDepthResponseDto updated = cleaningDepthService.changeStatus(cleaningDepthId, status, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updated, "Cleaning depth status updated successfully", ResponseEnum.SUCCESS);
    }

}
