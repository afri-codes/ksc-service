package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.JobChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JobCheckListItemRepository extends JpaRepository<JobChecklistItem, UUID> , JpaSpecificationExecutor<JobChecklistItem> {
}

