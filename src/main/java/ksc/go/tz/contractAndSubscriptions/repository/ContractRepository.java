package ksc.go.tz.contractAndSubscriptions.repository;

import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> , JpaSpecificationExecutor<Contract> {
    Optional<Contract> findById(UUID siteId);
}

