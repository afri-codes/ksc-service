package ksc.go.tz.leads.repository;

import ksc.go.tz.leads.entities.Leads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface LeadRepository extends JpaRepository<Leads, UUID>, JpaSpecificationExecutor<Leads> {
    Optional<Leads> findById(UUID leadId);
}
