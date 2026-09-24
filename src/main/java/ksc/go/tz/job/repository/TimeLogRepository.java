package ksc.go.tz.job.repository;

import ksc.go.tz.job.entities.Job;
import ksc.go.tz.job.entities.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TimeLogRepository extends JpaRepository<TimeLog, UUID> , JpaSpecificationExecutor<TimeLog> {

    List<TimeLog> findByJobIdAndStaffIdAndClockInNotNull(Job jpb, UUID userId);

    List<TimeLog> findByJobIdAndStaffIdAndClockOutNotNull(Job job, UUID userId);
}

