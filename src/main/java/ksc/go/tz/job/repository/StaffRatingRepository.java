package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.StaffRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StaffRatingRepository extends JpaRepository<StaffRating, UUID> , JpaSpecificationExecutor<StaffRating> {
}

