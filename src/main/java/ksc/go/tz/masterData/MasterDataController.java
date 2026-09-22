package ksc.go.tz.masterData;

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
public class MasterDataController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // GET /api/v1/reference/service-types

    // GET /api/v1/reference/frequencies

    // GET /api/v1/reference/job-types

    // GET /api/v1/reference/job-statuses

    // GET /api/v1/reference/payment-methods

    // GET /api/v1/reference/payment-statuses

    // GET /api/v1/reference/lead-sources

    // GET /api/v1/reference/lead-statuses

}
