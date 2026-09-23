package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CrewRepository extends JpaRepository<Crew, UUID> , JpaSpecificationExecutor<Crew> {
}

