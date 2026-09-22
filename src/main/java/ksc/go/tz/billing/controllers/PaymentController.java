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
public class PaymentController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/payments

    // GET /api/v1/payments

    // GET /api/v1/payments/{id}

    // GET /api/v1/payments/{id}/status

    // POST /api/v1/payments/{id}/verify

    // POST /api/v1/payments/{id}/refund

    // GET /api/v1/users/{userId}/payments

    // GET /api/v1/invoices/{invoiceId}/payments



}
