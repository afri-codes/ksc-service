package ksc.go.tz.masterData.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.masterData.dto.SlotDto;
import ksc.go.tz.masterData.dto.SlotResponseDto;
import ksc.go.tz.masterData.services.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SlotController {
    private final SlotService slotService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new slot")
    @Permission(name="SAVE NEW SLOT", code = "SAVE_SLOT")
    @PostMapping("/slots")
    public ApiResponseUtil.ApiResponseEntity<SlotResponseDto> saveSlot(@RequestBody @Valid SlotDto slotDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, slotService.addSlot(slotDto, createdBy), "Slot added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get all slot list ")
    @Permission(name="VIEW ALL SLOT", code = "VIEW_SLOT")
    @GetMapping("/slots")
    public ApiResponseUtil.ApiResponseEntity<List<SlotResponseDto>> getAll(Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(slotService.getAll(userId));
    }

    @Operation(summary = "get slot by id ")
    @Permission(name="VIEW SLOT BY ID", code = "VIEW_SLOT_BY_ID")
    @GetMapping("/slots/{id}")
    public ApiResponseUtil.ApiResponseEntity<SlotResponseDto> getById(@PathVariable("id") String slotId) {
        Optional<SlotResponseDto> slot = slotService.getById(slotId);
        if (slot.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Slot not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(slot.get());
    }

    @Operation(summary = "update slot ")
    @Permission(name="UPDATE SLOT", code = "UPDATE_SLOT")
    @PutMapping("/slots/{id}")
    public ApiResponseUtil.ApiResponseEntity<SlotResponseDto> updateSlot(@PathVariable("id") String slotId, @RequestBody @Valid SlotDto slotDto, Authentication authentication) {
        SlotResponseDto updatedSlot = slotService.updateSlot(slotId, slotDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSlot, "Slot updated successful", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "soft delete slot ")
    @Permission(name="DELETE SLOT", code = "DELETE_SLOT")
    @DeleteMapping("/slots/{id}")
    public ApiResponseUtil.ApiResponseEntity<SlotResponseDto> deleteById(@PathVariable("id") String slotId, Authentication authentication) {
        SlotResponseDto slot = slotService.deleteById(slotId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, slot, "Slot deleted successful", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "change slot status ")
    @Permission(name="CHANGE SLOT STATUS", code = "CHANGE_SLOT_STATUS")
    @PatchMapping("/slots/{id}/status")
    public ApiResponseUtil.ApiResponseEntity<SlotResponseDto> changeStatus(@PathVariable("id") String slotId, @RequestParam("status") String status, Authentication authentication) {
        SlotResponseDto updatedSlot = slotService.changeStatus(slotId, status, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSlot, "Slot status updated successfully", ResponseEnum.SUCCESS);
    }

}
