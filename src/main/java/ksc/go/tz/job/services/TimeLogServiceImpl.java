package ksc.go.tz.job.services;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.AfriException;
import jakarta.validation.Valid;
import ksc.go.tz.job.dto.TimeLogDto;
import ksc.go.tz.job.dto.TimeLogResponseDto;
import ksc.go.tz.job.entities.Job;
import ksc.go.tz.job.entities.TimeLog;
import ksc.go.tz.job.repository.JobRepository;
import ksc.go.tz.job.repository.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeLogServiceImpl implements TimeLogService {
    private final JobRepository jobRepository;
    private final TimeLogRepository timeLogRepository;
    private final AuthDetailsExtractor authDetailsExtractor;



    @Override
    public TimeLogResponseDto addClockInJob( TimeLogDto timeLogRequest, Authentication authentication) {
        Optional<Job> optionalJob = jobRepository.findById(UUID.fromString(timeLogRequest.getJobId()));
        if (optionalJob.isEmpty()) {
            throw new AfriException("Job not found");
        }

        List<TimeLog> timeLogs = timeLogRepository.findByJobIdAndStaffIdAndClockInNotNull(optionalJob.get(), authDetailsExtractor.getUserId(authentication));
        if (!timeLogs.isEmpty()) {
            throw new AfriException("Already has a clock in time log");
        }
        TimeLog timeLog = new TimeLog();
        timeLog.setJob(optionalJob.get());
        timeLog.setClockIn(LocalDateTime.now());
        timeLog.setGeofenceZoneIn(timeLogRequest.getGeofenceZone());
        return new TimeLogResponseDto(timeLogRepository.save(timeLog));
    }

    @Override
    public TimeLogResponseDto addClockOutJob( TimeLogDto timeLogRequest, Authentication authentication) {
        Optional<Job> optionalJob = jobRepository.findById(UUID.fromString(timeLogRequest.getJobId()));
        if (optionalJob.isEmpty()) {
            throw new AfriException("Job not found");
        }
        List<TimeLog> timeLogs = timeLogRepository.findByJobIdAndStaffIdAndClockOutNotNull(optionalJob.get(), authDetailsExtractor.getUserId(authentication));
        if (!timeLogs.isEmpty()) {
            throw new AfriException(" Already has a clock out time log");
        }
        TimeLog timeLog = new TimeLog();
        timeLog.setJob(optionalJob.get());
        timeLog.setClockOut(LocalDateTime.now());
        timeLog.setGeofenceZoneOut(timeLogRequest.getGeofenceZone());

        return new TimeLogResponseDto(timeLogRepository.save(timeLog));
    }

    @Override
    public TimeLogResponseDto getTimeLogsForJob(String jobId, Authentication authentication) {
        return null;
    }

    @Override
    public TimeLogResponseDto getTimeLogById(String timeLogId) {
        return null;
    }

    @Override
    public TimeLogResponseDto updateTimeLog(String timeLogId, @Valid TimeLogDto timeLogRequest, Authentication authentication) {
        return null;
    }

    @Override
    public TimeLogResponseDto getAttendanceByStaffId(String staffId, Authentication authentication) {
        return null;
    }
}
