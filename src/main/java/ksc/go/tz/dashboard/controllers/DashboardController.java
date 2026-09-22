package ksc.go.tz.dashboard.controllers;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // GET /api/v1/dashboard/admin
//    {
//        "totalCustomers": 120,
//            "activeContracts": 85,
//            "activeSubscriptions": 71,
//            "pendingQuotes": 12,
//            "scheduledJobs": 24,
//            "completedJobs": 350,
//            "pendingPayments": 18,
//            "revenue": 125000000
//    }

    // GET /api/v1/dashboard/customer

    // GET /api/v1/dashboard/staff

    // GET /api/v1/dashboard/supervisor

}
