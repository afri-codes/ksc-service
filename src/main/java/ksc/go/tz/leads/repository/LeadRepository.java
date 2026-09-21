package ksc.go.tz.leads.repository;

import ksc.go.tz.leads.entities.Leads;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Leads, UUID> {
    Optional<Leads> findById(UUID leadId);
}

