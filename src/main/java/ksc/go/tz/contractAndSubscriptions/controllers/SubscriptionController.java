package ksc.go.tz.contractAndSubscriptions.controllers;

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
public class SubscriptionController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // POST /api/v1/subscriptions

    // GET /api/v1/subscriptions

    // GET /api/v1/subscriptions/{id}

    // PUT /api/v1/subscriptions/{id}

    // POST /api/v1/subscriptions/{id}/cancel

    // POST /api/v1/subscriptions/{id}/pause

    // POST /api/v1/subscriptions/{id}/resume

    // POST /api/v1/subscriptions/{id}/renew

    // POST /api/v1/subscriptions/{id}/generate-job

    // GET /api/v1/subscriptions/{id}/invoices

    // GET /api/v1/subscriptions/{id}/jobs

    // GET /api/v1/subscriptions/{id}/invoices


}
