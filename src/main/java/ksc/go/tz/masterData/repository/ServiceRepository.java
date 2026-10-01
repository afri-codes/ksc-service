package ksc.go.tz.masterData.repository;

import ksc.go.tz.masterData.entities.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findById(UUID siteId);

    boolean existsByServiceNameIgnoreCase(String serviceName);
}

