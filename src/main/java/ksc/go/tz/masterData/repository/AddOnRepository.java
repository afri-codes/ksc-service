package ksc.go.tz.masterData.repository;

import ksc.go.tz.masterData.entities.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddOnRepository extends JpaRepository<AddOn, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
