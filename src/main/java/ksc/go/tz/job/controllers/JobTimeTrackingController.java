package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.job.dto.TimeLogDto;
import ksc.go.tz.job.dto.TimeLogResponseDto;
import ksc.go.tz.job.services.TimeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobTimeTrackingController {
    private final TimeLogService timeLogService;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/jobs/{jobId}/clock-in
    @Operation(summary = "Clock in for a job")
    @Permission(name = "Clock In", code = "CLOCK_IN")
    @PostMapping("/jobs/{jobId}/clock-in")
    public ApiResponseUtil.ApiResponseEntity<TimeLogResponseDto> clockInForJob(@RequestBody @Valid TimeLogDto timeLogDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, timeLogService.addClockInJob(timeLogDto, authentication) , "Clocked in successfully", null);
    }

    // POST /api/v1/jobs/{jobId}/clock-out
    @Operation(summary = "Clock out for a job")
    @Permission(name = "Clock Out", code = "CLOCK_OUT")
    @PostMapping("/jobs/{jobId}/clock-out")
    public ApiResponseUtil.ApiResponseEntity<TimeLogResponseDto> clockOutForJob(@RequestBody @Valid TimeLogDto timeLogDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, timeLogService.addClockOutJob(timeLogDto, authentication) , "Clocked out successfully", null);
    }

    // GET /api/v1/jobs/{jobId}/time-logs
    @Operation(summary = "Get time logs for a job")
    @Permission(name = "View Time Logs", code = "VIEW_TIME_LOGS")
    @GetMapping("/jobs/{jobId}/time-logs")
    public ApiResponseUtil.ApiResponseEntity<TimeLogResponseDto> getTimeLogsForJob(@PathVariable("jobId") String jobId, Authentication authentication) {
        return apiResponseUtil.getResponse(timeLogService.getTimeLogsForJob(jobId, authentication));
    }

    // GET /api/v1/time-logs/{id}
    @Operation(summary = "Get a time log by ID")
    @Permission(name = "View Time Log By ID", code = "VIEW_TIME_LOG_BY_ID")
    @GetMapping("/time-logs/{id}")
    public ApiResponseUtil.ApiResponseEntity<TimeLogResponseDto> getTimeLogById(@PathVariable("id") String timeLogId) {
        return apiResponseUtil.getResponse(timeLogService.getTimeLogById(timeLogId));
    }

    // PUT /api/v1/time-logs/{id}
    @Operation(summary = "Update a time log")
    @Permission(name = "Update Time Log", code = "UPDATE_TIME_LOG")
    @PutMapping("/time-logs/{id}")
    public ApiResponseUtil.ApiResponseEntity<TimeLogResponseDto> updateTimeLog(@PathVariable("id") String timeLogId, @RequestBody @Valid TimeLogDto timeLogDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, timeLogService.updateTimeLog(timeLogId, timeLogDto, authentication), "Time log updated successfully", null);
    }

    // GET /api/v1/staff/{staffId}/attendance
    @Operation(summary = "Get attendance for a specific staff member")
    @Permission(name = "View Staff Attendance", code = "VIEW_STAFF_ATTENDANCE")
    @GetMapping("/staff/{staffId}/attendance")
    public ApiResponseUtil.ApiResponseEntity<TimeLogResponseDto> getAttendanceByStaffId(@PathVariable("staffId") String staffId, Authentication authentication) {
        return apiResponseUtil.getResponse(timeLogService.getAttendanceByStaffId(staffId, authentication));
    }

}
