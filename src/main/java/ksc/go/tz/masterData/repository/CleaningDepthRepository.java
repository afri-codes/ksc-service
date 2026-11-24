package ksc.go.tz.masterData.repository;

import ksc.go.tz.masterData.entities.CleaningDepth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CleaningDepthRepository extends JpaRepository<CleaningDepth, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
