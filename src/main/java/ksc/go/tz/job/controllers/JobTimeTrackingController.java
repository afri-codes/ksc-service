package ksc.go.tz.job.controllers;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobTimeTrackingController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/jobs/{jobId}/clock-in

    // POST /api/v1/jobs/{jobId}/clock-out

    // GET /api/v1/jobs/{jobId}/time-logs

    // GET /api/v1/time-logs/{id}

    // PUT /api/v1/time-logs/{id}

    // GET /api/v1/staff/{staffId}/attendance

}
