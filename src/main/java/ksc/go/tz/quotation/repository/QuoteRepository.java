package ksc.go.tz.quotation.repository;

import java.util.Collection;
import java.util.List;
import ksc.go.tz.quotation.entities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    Optional<Quote> findById(UUID siteId);

    List<Quote> findBySiteIdOrderByCreatedAtDesc(UUID siteId);

    List<Quote> findBySiteIdInOrderByCreatedAtDesc(Collection<UUID> siteIds);
}

