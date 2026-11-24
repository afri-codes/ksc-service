package ksc.go.tz.job.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.job.dto.CrewDto;
import ksc.go.tz.job.dto.CrewResponseDto;
import ksc.go.tz.job.entities.Crew;
import ksc.go.tz.job.repository.CrewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrewServiceImpl implements CrewService {
    private final CrewRepository crewRepository;


    @Override
    public CrewResponseDto createCrew(CrewDto crewDto, UUID createdBy) {
        Crew crew = new Crew();
        crew.setCrewName(crewDto.getCrewName());
        crew.setSupervisorId(crewDto.getSupervisorId());
        crew.setSupervisorName(null);
        crew.setZone(crewDto.getZoneName());
        crew.setCreatedBy(createdBy);
        return new CrewResponseDto(crewRepository.save(crew));
    }

    @Override
    public List<CrewResponseDto> getAllCrews(Authentication authentication) {
        return crewRepository.findAll().stream().map(CrewResponseDto::new).toList();

    }

    @Override
    public CrewResponseDto getCrewById(UUID crewId, Authentication authentication) {
        Optional<Crew> crewOptional = crewRepository.findById(crewId);
        if (crewOptional.isEmpty()) {
            throw new AfriException("Crew not found with id: " + crewId);
        }
         return new CrewResponseDto(crewOptional.get());
    }

    @Override
    public CrewResponseDto updateCrewById(UUID crewId, CrewDto crewDto, Authentication authentication) {
        Optional<Crew> crewOptional = crewRepository.findById(crewId);
        if (crewOptional.isEmpty()) {
            throw new AfriException("Crew not found with id: " + crewId);
        }
        Crew crew = crewOptional.get();
        crew.setCrewName(crewDto.getCrewName());
        crew.setSupervisorId(crewDto.getSupervisorId());
        crew.setZone(crewDto.getZoneName());
        return new CrewResponseDto(crewRepository.save(crew));
    }

    @Override
    public CrewResponseDto deleteCrewById(UUID crewId, Authentication authentication) {
        Optional<Crew> crewOptional = crewRepository.findById(crewId);
        if (crewOptional.isEmpty()) {
            throw new AfriException("Crew not found with id: " + crewId);
        }
        Crew crew = crewOptional.get();
        crew.setDeleted(true);
        crew.setDeletedAt(LocalDateTime.now());
        return crewRepository.save(crew) != null ? new CrewResponseDto(crew) : null;
    }

    @Override
    public CrewResponseDto assignSupervisorToCrew(UUID crewId, UUID supervisorId, Authentication authentication) {
        return null;
    }
}
