package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.Feedback;
import ksc.go.tz.job.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> , JpaSpecificationExecutor<Feedback> {
    List<Feedback> findByJob(Job job);
}

