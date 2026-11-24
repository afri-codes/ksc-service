package ksc.go.tz.job.services;


import ksc.go.tz.job.dto.CrewDto;
import ksc.go.tz.job.dto.CrewResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface CrewService {

    CrewResponseDto createCrew(CrewDto crewDto, UUID createdBy);

    List<CrewResponseDto> getAllCrews(Authentication authentication);

    CrewResponseDto getCrewById(UUID crewId, Authentication authentication);

    CrewResponseDto updateCrewById(UUID crewId, CrewDto crewDto, Authentication authentication);

    CrewResponseDto deleteCrewById(UUID crewId, Authentication authentication);

    CrewResponseDto assignSupervisorToCrew(UUID crewId, UUID supervisorId, Authentication authentication);
}
