package ksc.go.tz.sitesAndAssests.services;



import ksc.go.tz.sitesAndAssests.dto.SiteDto;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteService {

    List<SiteResponseDto> getAll(UUID userId);

    SiteResponseDto deleteById(String sitesId, UUID userId);

    Optional<SiteResponseDto> getById(String sitesId);

    SiteResponseDto updateSite(String site, SiteDto siteDto, UUID userId);

    SiteResponseDto addSite(SiteDto siteDto, UUID createdBy);
}
