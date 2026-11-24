package ksc.go.tz.dashboard.controllers;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // GET /api/v1/reports/revenue

    // GET /api/v1/reports/payments

    // GET /api/v1/reports/jobs

    // GET /api/v1/reports/jobs

    // GET /api/v1/reports/staff-performance

    // GET /api/v1/reports/customers

    // GET /api/v1/reports/contracts

    // GET /api/v1/reports/subscriptions

    // GET /api/v1/reports/{report}/export  supporting pdf, excel, csv

}
