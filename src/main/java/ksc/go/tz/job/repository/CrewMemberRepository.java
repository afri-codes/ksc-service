package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrewMemberRepository extends JpaRepository<CrewMember, UUID> {

    List<CrewMember> findByCrewIdOrderByStaffNameAsc(UUID crewId);

    List<CrewMember> findByStaffId(String staffId);

    Optional<CrewMember> findByCrewIdAndStaffId(UUID crewId, String staffId);
}
