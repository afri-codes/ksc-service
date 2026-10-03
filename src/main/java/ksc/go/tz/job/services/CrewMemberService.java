package ksc.go.tz.job.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.job.dto.CrewMemberDto;
import ksc.go.tz.job.dto.CrewMemberResponseDto;
import ksc.go.tz.job.entities.Crew;
import ksc.go.tz.job.entities.CrewMember;
import ksc.go.tz.job.repository.CrewMemberRepository;
import ksc.go.tz.job.repository.CrewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CrewMemberService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;

    public CrewMemberResponseDto addMember(String crewId, CrewMemberDto dto, UUID userId) {
        Crew crew = findCrew(crewId);
        String staffId = dto.getStaffId().trim();
        if (crewMemberRepository.findByCrewIdAndStaffId(crew.getId(), staffId).isPresent()) {
            throw new AfriException("Staff member " + staffId + " is already in crew " + crew.getCrewName());
        }
        CrewMember member = new CrewMember();
        member.setCrew(crew);
        member.setStaffId(staffId);
        member.setStaffName(blankToNull(dto.getStaffName()));
        member.setMemberRole(blankToNull(dto.getMemberRole()));
        member.setJoinedOn(dto.getJoinedOn() != null ? dto.getJoinedOn() : LocalDate.now());
        member.setCreatedBy(userId);
        member.setCreatedAt(LocalDateTime.now());
        return new CrewMemberResponseDto(crewMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<CrewMemberResponseDto> getMembers(String crewId) {
        Crew crew = findCrew(crewId);
        return crewMemberRepository.findByCrewIdOrderByStaffNameAsc(crew.getId()).stream()
                .map(CrewMemberResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<CrewMemberResponseDto> getCrewsOfStaff(String staffId) {
        return crewMemberRepository.findByStaffId(staffId.trim()).stream().map(CrewMemberResponseDto::new).toList();
    }

    public CrewMemberResponseDto removeMember(String crewId, String staffId, UUID userId) {
        Crew crew = findCrew(crewId);
        CrewMember member = crewMemberRepository.findByCrewIdAndStaffId(crew.getId(), staffId.trim())
                .orElseThrow(() -> new AfriException("Staff member " + staffId + " is not in crew " + crew.getCrewName()));
        member.setDeletedAt(LocalDateTime.now());
        member.setDeleted(true);
        member.setUpdatedBy(userId);
        member.setUpdatedAt(LocalDateTime.now());
        return new CrewMemberResponseDto(crewMemberRepository.save(member));
    }

    private Crew findCrew(String crewId) {
        UUID id;
        try {
            id = UUID.fromString(crewId);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AfriException("Invalid crew ID: " + crewId);
        }
        return crewRepository.findById(id).orElseThrow(() -> new AfriException("Crew not found"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
