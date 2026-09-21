package ksc.go.tz.sitesAndAssests.repository;

import ksc.go.tz.sitesAndAssests.entities.Sites;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Sites, UUID> {
    Optional<Sites> findById(UUID siteId);
}

