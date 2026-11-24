package ksc.go.tz.contractAndSubscriptions.repository;

import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> , JpaSpecificationExecutor<Contract> {
    Optional<Contract> findById(UUID siteId);

    boolean existsByQuoteId(UUID quoteId);

    List<Contract> findByStatusAndStartDateLessThanEqual(ContractStatus status, LocalDate date);

    List<Contract> findByStatusAndEndDateBefore(ContractStatus status, LocalDate date);

    List<Contract> findByStatusAndEndDateBetweenOrderByEndDateAsc(ContractStatus status, LocalDate from, LocalDate to);
}
