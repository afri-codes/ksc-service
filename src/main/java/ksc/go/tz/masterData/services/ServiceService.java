package ksc.go.tz.masterData.services;

import ksc.go.tz.masterData.dto.ServiceDto;
import ksc.go.tz.masterData.dto.ServiceResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceService {

    List<ServiceResponseDto> getAll(UUID userId);

    ServiceResponseDto deleteById(String sitesId, UUID userId);

    Optional<ServiceResponseDto> getById(String sitesId);

    ServiceResponseDto updateService(String site, ServiceDto siteDto, UUID userId);

    ServiceResponseDto addService(ServiceDto siteDto, UUID createdBy);

    ServiceResponseDto changeStatus(String serviceId, String status, UUID userId);
}
