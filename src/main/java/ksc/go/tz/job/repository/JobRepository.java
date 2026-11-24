package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> , JpaSpecificationExecutor<Job> {
}

