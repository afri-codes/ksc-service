package ksc.go.tz.billing.controllers;

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
public class InvoiceController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

// POST /api/v1/invoices

    // GET /api/v1/invoices

    // GET /api/v1/invoices/{id}


    // PUT /api/v1/invoices/{id}

    // POST /api/v1/invoices/{id}/cancel

    // POST /api/v1/invoices/{id}/send


    // GET /api/v1/invoices/{id}/pdf

    // GET /api/v1/invoices/{id}/payments

    // POST /api/v1/invoices/{id}/mark-paid

}
