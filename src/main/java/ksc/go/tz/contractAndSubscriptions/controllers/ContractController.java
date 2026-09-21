package ksc.go.tz.contractAndSubscriptions.controllers;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService siteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;



}
