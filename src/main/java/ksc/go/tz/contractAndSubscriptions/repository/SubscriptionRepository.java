package ksc.go.tz.contractAndSubscriptions.repository;

import ksc.go.tz.contractAndSubscriptions.entities.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> , JpaSpecificationExecutor<Subscription> {
    Optional<Subscription> findById(UUID subscriptionId);
}

