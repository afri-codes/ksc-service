package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.JobCheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JobCheckListRepository extends JpaRepository<JobCheckList, UUID> , JpaSpecificationExecutor<JobCheckList> {
}

