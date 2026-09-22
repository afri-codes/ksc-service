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
public class FeedbackController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/feedback


    // GET /api/v1/feedback

    // GET /api/v1/feedback/{id}

    // PUT /api/v1/feedback/{id}


    // DELETE /api/v1/feedback/{id}

    // GET /api/v1/jobs/{jobId}/feedback

    // GET /api/v1/users/{userId}/feedback

}
