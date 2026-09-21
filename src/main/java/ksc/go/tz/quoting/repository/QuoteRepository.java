package ksc.go.tz.quoting.repository;

import ksc.go.tz.quoting.entities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    Optional<Quote> findById(UUID siteId);
}

