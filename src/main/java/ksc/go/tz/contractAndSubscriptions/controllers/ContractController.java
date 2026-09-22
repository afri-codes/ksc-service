package ksc.go.tz.contractAndSubscriptions.controllers;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // POST /api/v1/contracts

    // GET /api/v1/contracts with pagination, sorting and filtering by status, source and service line

    // PUT /api/v1/contracts/{id}

    // DELETE /api/v1/contracts/{id}

    // POST /api/v1/contracts/{id}/send

   // POST /api/v1/contracts/{id}/reject

    // POST /api/v1/contracts/{id}/approve

    // GET /api/v1/contracts/{id}/download

    // GET /api/v1/contracts/{id}/subscriptions

    // GET /api/v1/contracts/{id}/jobs

    // GET /api/v1/contracts/{id}/invoices

    // GET /api/v1/contracts/{id}/payments

}
