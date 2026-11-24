package ksc.go.tz.masterData.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.masterData.dto.ServiceDto;
import ksc.go.tz.masterData.dto.ServiceResponseDto;
import ksc.go.tz.masterData.services.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Services", description = "Master data: services offered (Cleaning, Fumigation, Property Management)")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new service")
    @Permission(name="SAVE NEW SERVICE", code = "SAVE_SERVICE")
    @PostMapping("/services")
    public  ApiResponseUtil.ApiResponseEntity<ServiceResponseDto> saveService(@RequestBody @Valid ServiceDto serviceDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, siteService.addService(serviceDto, createdBy), "Service added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get all service list ")
    @Permission(name="VIEW ALL SERVICE", code = "VIEW_SERVICE")
    @GetMapping("/services")
    public ApiResponseUtil.ApiResponseEntity<List<ServiceResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(siteService.getAll(userId));

    }

    @Operation(summary = "get service by id ")
    @Permission(name="VIEW SERVICE BY ID", code = "VIEW_SERVICE_BY_ID")
    @GetMapping("/services/{id}")
    public ApiResponseUtil.ApiResponseEntity<ServiceResponseDto> getById(@PathVariable("id") String sericeId){
        Optional<ServiceResponseDto> site = siteService.getById(sericeId);
        if (site.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Service not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(site.get());
    }

    @Operation(summary = "update service ")
    @Permission(name="UPDATE SERVICE", code = "UPDATE_SERVICE")
    @PutMapping("/service/{id}")
    public ApiResponseUtil.ApiResponseEntity<ServiceResponseDto> updateService(@PathVariable("id") String serviceId, @RequestBody ServiceDto serviceDto, Authentication authentication) {
        ServiceResponseDto updatedSite = siteService.updateService(serviceId, serviceDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSite,"Service updated successful",ResponseEnum.SUCCESS);

    }
    @Operation(summary = "soft delete service ")
    @Permission(name="DELETE SERVICE", code = "DELETE_SERVICE")
    @DeleteMapping("/services/{id}")
    public ApiResponseUtil.ApiResponseEntity<ServiceResponseDto> deleteById(@PathVariable("id") String serviceId, Authentication authentication){
        ServiceResponseDto sites =  siteService.deleteById(serviceId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, sites,"Service deleted successful",ResponseEnum.SUCCESS);

    }

    @Operation(summary = "change service status ", description = "Set status to ACTIVE or INACTIVE via the 'status' query parameter (case-insensitive).")
    @Permission(name="CHANGE SERVICE STATUS", code = "CHANGE_SERVICE_STATUS")
    @PatchMapping("/services/{serviceId}/status")
    public ApiResponseUtil.ApiResponseEntity<ServiceResponseDto> changeStatus(@PathVariable("serviceId") String serviceId, @RequestParam("status") String status, Authentication authentication) {
        ServiceResponseDto updatedSite = siteService.changeStatus(serviceId, status, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSite, "Service status updated successfully", ResponseEnum.SUCCESS);
    }

}
