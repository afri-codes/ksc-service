package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.job.dto.JobDto;
import ksc.go.tz.job.dto.JobResponseDto;
import ksc.go.tz.job.services.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // POST /api/v1/jobs
    @Operation(summary = "Create a new job")
    @Permission(name = "Create Job", code = "CREATE_JOB")
    public ApiResponseUtil.ApiResponseEntity<JobResponseDto> createJob(@RequestBody @Valid JobDto jobDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, jobService.createJob(jobDto, createdBy), "Job created successfully", null);
    }

    // GET /api/v1/jobs
    @Operation(summary = "Get all jobs")
    @Permission(name = "View All Jobs", code = "VIEW_ALL_JOBS")
    public ApiResponseUtil.ApiResponseEntity<List<JobResponseDto>> getAllJobs(Authentication authentication) {
        return apiResponseUtil.getResponse(jobService.getAllJobs(authentication));
    }

    // GET /api/v1/jobs/{id}
    @Operation(summary = "Get job by ID")
    @Permission(name = "View Job By ID", code = "VIEW_JOB_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<JobResponseDto> getJobById(UUID jobId, Authentication authentication) {
        return apiResponseUtil.getResponse(jobService.getJobById(jobId, authentication));
    }

    // PUT /api/v1/jobs/{id}
    @Operation(summary = "Update job by ID")
    @Permission(name = "Update Job By ID", code = "UPDATE_JOB_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<JobResponseDto> updateJobById(UUID jobId, @RequestBody @Valid JobDto jobDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, jobService.updateJobById(jobId, jobDto, authentication), "Job updated successfully", null);
    }

    // POST /api/v1/jobs/{id}/cancel

    // POST /api/v1/jobs/{id}/assign-crew

    // POST /api/v1/jobs/{id}/reassign-crew

    // POST /api/v1/jobs/{id}/start

    // POST /api/v1/jobs/{id}/pause

    // POST /api/v1/jobs/{id}/resume

    // POST /api/v1/jobs/{id}/pause

}
