package ksc.go.tz.sitesAndAssests.services;

import afriUtils.responses.AfriException;

import ksc.go.tz.sitesAndAssests.dto.SiteDto;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import ksc.go.tz.sitesAndAssests.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    @Override
    public SiteResponseDto addSite(SiteDto siteDto, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Sites sites = new Sites();
        sites.setSiteType(siteDto.getSiteType());
        sites.setAreaSqm(siteDto.getAreaSqm());
        sites.setRoomCount(siteDto.getRoomCount());
        sites.setAddressArea(siteDto.getAddressArea());
        sites.setLatitude(siteDto.getLatitude());
        sites.setLongitude(siteDto.getLongitude());
        Map<String, Object> plotCoordinates = new HashMap<>();
        plotCoordinates.put("latitude", Double.valueOf(siteDto.getLatitude()));
        plotCoordinates.put("longitude", Double.valueOf(siteDto.getLongitude()));
        sites.setPlotCoordinates(plotCoordinates);
        sites.setSecured(siteDto.getSecured());
        sites.setAccessType(siteDto.getAccessType());
        sites.setCreatedBy(createdBy);
        sites.setSite_owner(String.valueOf(createdBy.toString()));
        sites.setCreatedAt(now);
        return new SiteResponseDto(siteRepository.save(sites));
    }

    @Override
    public List<SiteResponseDto> getAll(UUID userId) {
        return siteRepository.findAll().stream().map(SiteResponseDto::new).toList();

    }

    @Override
    public Optional<SiteResponseDto> getById(String siteId) {
        Optional<Sites> site = siteRepository.findById(UUID.fromString(siteId));
        if (site.isEmpty()) {
            throw new AfriException("Site not found");
        }
        return siteRepository.findById(UUID.fromString(siteId)).map(SiteResponseDto::new);

    }

    @Override
    public SiteResponseDto updateSite(String site, SiteDto siteDto, UUID userId) {

        UUID siteId;

        try {
            siteId = UUID.fromString(site);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid site ID: " + site);
        }

        Sites existingSite = siteRepository.findById(siteId)
                .orElseThrow(() -> new AfriException("Site not found"));

        existingSite.setSiteType(siteDto.getSiteType());
        existingSite.setAreaSqm(siteDto.getAreaSqm());
        existingSite.setRoomCount(siteDto.getRoomCount());
        existingSite.setAddressArea(siteDto.getAddressArea());
//        existingSite.setPlotCoordinates(siteDto.getPlotCoordinates());
        existingSite.setLatitude(siteDto.getLatitude());
        existingSite.setLongitude(siteDto.getLongitude());
        existingSite.setSecured(siteDto.getSecured());
        existingSite.setAccessType(siteDto.getAccessType());
        existingSite.setUpdatedBy(userId);
        existingSite.setUpdatedAt(LocalDateTime.now());

        Sites updatedSite = siteRepository.save(existingSite);

        return new SiteResponseDto(updatedSite);
    }

    @Override
    public SiteResponseDto deleteById(String containerTypeId, UUID userId) {
        Optional<Sites> sites = siteRepository.findById(UUID.fromString(containerTypeId));
        if(sites.isPresent()){
            Sites sites1 = sites.get();
            sites1.setDeletedAt(LocalDateTime.now());
            return new SiteResponseDto(siteRepository.save(sites1));
        } else {
            throw new AfriException("Site not found");
        }
    }



}
