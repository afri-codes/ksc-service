package ksc.go.tz.job.services;


import jakarta.validation.Valid;
import ksc.go.tz.job.dto.TimeLogDto;
import ksc.go.tz.job.dto.TimeLogResponseDto;
import org.springframework.security.core.Authentication;


public interface TimeLogService {


    TimeLogResponseDto addClockInJob(@Valid TimeLogDto timeLogRequest, Authentication authentication);

    TimeLogResponseDto addClockOutJob(@Valid TimeLogDto timeLogRequest, Authentication authentication);

    TimeLogResponseDto getTimeLogsForJob(String jobId, Authentication authentication);

    TimeLogResponseDto getTimeLogById(String timeLogId);

    TimeLogResponseDto updateTimeLog(String timeLogId, @Valid TimeLogDto timeLogRequest, Authentication authentication);

    TimeLogResponseDto getAttendanceByStaffId(String staffId, Authentication authentication);
}