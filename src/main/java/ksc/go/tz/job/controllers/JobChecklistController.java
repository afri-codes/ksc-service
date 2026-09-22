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
public class JobChecklistController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/jobs/{jobId}/checklists

    // GET /api/v1/jobs/{jobId}/checklists

    // GET /api/v1/checklists/{id}

    // PUT /api/v1/checklists/{id}

    // DELETE /api/v1/checklists/{id}

}
