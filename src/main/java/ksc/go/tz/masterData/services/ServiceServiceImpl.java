package ksc.go.tz.masterData.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.dto.ServiceDto;
import ksc.go.tz.masterData.dto.ServiceResponseDto;
import ksc.go.tz.masterData.entities.Service;
import ksc.go.tz.masterData.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    @Override
    public ServiceResponseDto addService(ServiceDto siteDto, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Service service = new Service();
        service.setServiceName(siteDto.getServiceName());
        service.setStatus(Status.ACTIVE);
        service.setCreatedBy(createdBy);
        service.setCreatedAt(now);
        return new ServiceResponseDto(serviceRepository.save(service));
    }

    @Override
    public ServiceResponseDto changeStatus(String serviceId, String status, UUID userId) {
        Optional<Service> optionalService = serviceRepository.findById(UUID.fromString(serviceId));
        if (optionalService.isPresent()) {
            Service service = optionalService.get();
            if (status.equalsIgnoreCase("ACTIVE")) {
                service.setStatus(Status.ACTIVE);
            } else if (status.equalsIgnoreCase("INACTIVE")) {
                service.setStatus(Status.INACTIVE);
            } else {
                throw new AfriException("Invalid status value: " + status);
            }
            service.setUpdatedBy(userId);
            service.setUpdatedAt(LocalDateTime.now());
            return new ServiceResponseDto(serviceRepository.save(service));
        } else {
            throw new AfriException("Service not found");
        }
    }

    @Override
    public List<ServiceResponseDto> getAll(UUID userId) {
        return serviceRepository.findAll().stream().map(ServiceResponseDto::new).toList();

    }

    @Override
    public Optional<ServiceResponseDto> getById(String siteId) {
        Optional<Service> site = serviceRepository.findById(UUID.fromString(siteId));
        if (site.isEmpty()) {
            throw new AfriException("Site not found");
        }
        return serviceRepository.findById(UUID.fromString(siteId)).map(ServiceResponseDto::new);

    }

    @Override
    public ServiceResponseDto updateService(String site, ServiceDto siteDto, UUID userId) {
        UUID siteId;

        try {
            siteId = UUID.fromString(site);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid site ID: " + site);
        }

        Service existingService = serviceRepository.findById(siteId)
                .orElseThrow(() -> new AfriException("Site not found"));
        existingService.setServiceName(siteDto.getServiceName());
        existingService.setUpdatedBy(userId);
        existingService.setUpdatedAt(LocalDateTime.now());

        Service updatedSite = serviceRepository.save(existingService);

        return new ServiceResponseDto(updatedSite);
    }

    @Override
    public ServiceResponseDto deleteById(String containerTypeId, UUID userId) {
        Optional<Service> optionalService = serviceRepository.findById(UUID.fromString(containerTypeId));
        if(optionalService.isPresent()){
            Service service = optionalService.get();
            service.setDeletedAt(LocalDateTime.now());
            return new ServiceResponseDto(serviceRepository.save(service));
        } else {
            throw new AfriException("Site not found");
        }
    }



}
