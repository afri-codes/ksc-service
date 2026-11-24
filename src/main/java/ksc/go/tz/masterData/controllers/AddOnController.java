package ksc.go.tz.masterData.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.masterData.dto.AddOnDto;
import ksc.go.tz.masterData.dto.AddOnResponseDto;
import ksc.go.tz.masterData.services.AddOnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Add-ons", description = "Master data: optional extra services, each with its own price")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AddOnController {
    private final AddOnService addOnService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new add-on", description = "Creates an add-on. Name must be unique (case-insensitive) and price must not be negative. New add-ons are ACTIVE.")
    @Permission(name="SAVE NEW ADD ON", code = "SAVE_ADD_ON")
    @PostMapping("/add-ons")
    public ApiResponseUtil.ApiResponseEntity<AddOnResponseDto> saveAddOn(@RequestBody @Valid AddOnDto addOnDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, addOnService.addAddOn(addOnDto, createdBy), "Add-on added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get all add-on list ")
    @Permission(name="VIEW ALL ADD ON", code = "VIEW_ADD_ON")
    @GetMapping("/add-ons")
    public ApiResponseUtil.ApiResponseEntity<List<AddOnResponseDto>> getAll(Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(addOnService.getAll(userId));
    }

    @Operation(summary = "get add-on by id ")
    @Permission(name="VIEW ADD ON BY ID", code = "VIEW_ADD_ON_BY_ID")
    @GetMapping("/add-ons/{id}")
    public ApiResponseUtil.ApiResponseEntity<AddOnResponseDto> getById(@PathVariable("id") String addOnId) {
        Optional<AddOnResponseDto> addOn = addOnService.getById(addOnId);
        if (addOn.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Add-on not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(addOn.get());
    }

    @Operation(summary = "update add-on ", description = "Updates name, price and description. Existing sites keep their stored total price until they are updated.")
    @Permission(name="UPDATE ADD ON", code = "UPDATE_ADD_ON")
    @PutMapping("/add-ons/{id}")
    public ApiResponseUtil.ApiResponseEntity<AddOnResponseDto> updateAddOn(@PathVariable("id") String addOnId, @RequestBody @Valid AddOnDto addOnDto, Authentication authentication) {
        AddOnResponseDto updated = addOnService.updateAddOn(addOnId, addOnDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updated, "Add-on updated successful", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "soft delete add-on ")
    @Permission(name="DELETE ADD ON", code = "DELETE_ADD_ON")
    @DeleteMapping("/add-ons/{id}")
    public ApiResponseUtil.ApiResponseEntity<AddOnResponseDto> deleteById(@PathVariable("id") String addOnId, Authentication authentication) {
        AddOnResponseDto deleted = addOnService.deleteById(addOnId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, deleted, "Add-on deleted successful", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "change add-on status ", description = "Set status to ACTIVE or INACTIVE via the 'status' query parameter (case-insensitive). INACTIVE add-ons cannot be selected for sites.")
    @Permission(name="CHANGE ADD ON STATUS", code = "CHANGE_ADD_ON_STATUS")
    @PatchMapping("/add-ons/{id}/status")
    public ApiResponseUtil.ApiResponseEntity<AddOnResponseDto> changeStatus(@PathVariable("id") String addOnId, @RequestParam("status") String status, Authentication authentication) {
        AddOnResponseDto updated = addOnService.changeStatus(addOnId, status, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updated, "Add-on status updated successfully", ResponseEnum.SUCCESS);
    }

}
