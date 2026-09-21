package ksc.go.tz.contractAndSubscriptions.repository;

import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Optional<Contract> findById(UUID siteId);
}

