package ksc.go.tz.contractAndSubscriptions.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;
import ksc.go.tz.contractAndSubscriptions.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // POST /api/v1/subscriptions
    @Operation(summary = "Save or add new subscription")
    @Permission(name="SAVE NEW SUBSCRIPTION", code = "SAVE_SUBSCRIPTION")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> addSubscription(@RequestBody @Valid SubscriptionResponseDto subscriptionDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, subscriptionService.addSubscription(subscriptionDto, createdBy), "Subscription added successfully", null);
    }

    // GET /api/v1/subscriptions
    @Operation(summary = "Get all subscriptions")
    @Permission(name = "VIEW ALL SUBSCRIPTIONS", code = "VIEW_SUBSCRIPTIONS")
    public ApiResponseUtil.ApiResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions(Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(subscriptionService.getAll(userId));
    }

    // GET /api/v1/subscriptions/{id}
    @Operation(summary = "Get subscription by ID")
    @Permission(name = "VIEW SUBSCRIPTION BY ID", code = "VIEW_SUBSCRIPTION_BY_ID")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> getSubscriptionById(String subscriptionId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(subscriptionService.getSubscriptionById(subscriptionId, userId));
    }

    // PUT /api/v1/subscriptions/{id}
    @Operation(summary = "Update subscription")
    @Permission(name = "UPDATE SUBSCRIPTION", code = "UPDATE_SUBSCRIPTION")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> updateSubscription(String subscriptionId, SubscriptionResponseDto subscriptionDto, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, subscriptionService.updateSubscription(subscriptionId, subscriptionDto, userId), "Subscription updated successfully", null);
    }

    // POST /api/v1/subscriptions/{id}/cancel
    @Operation(summary = "Cancel subscription")
    @Permission(name = "CANCEL SUBSCRIPTION", code = "CANCEL_SUBSCRIPTION")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> cancelSubscription(String subscriptionId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, subscriptionService.cancelSubscription(subscriptionId, userId), "Subscription canceled successfully", null);
    }

    // POST /api/v1/subscriptions/{id}/pause
    @Operation(summary = "Pause subscription")
    @Permission(name = "PAUSE SUBSCRIPTION", code = "PAUSE_SUBSCRIPTION")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> pauseSubscription(String subscriptionId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, subscriptionService.pauseSubscription(subscriptionId, userId), "Subscription paused successfully", null);
    }

    // POST /api/v1/subscriptions/{id}/resume
    @Operation(summary = "Resume subscription")
    @Permission(name = "RESUME SUBSCRIPTION", code = "RESUME_SUBSCRIPTION")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> resumeSubscription(String subscriptionId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, subscriptionService.resumeSubscription(subscriptionId, userId), "Subscription resumed successfully", null);
    }

    // POST /api/v1/subscriptions/{id}/renew
    @Operation(summary = "Renew subscription")
    @Permission(name = "RENEW SUBSCRIPTION", code = "RENEW_SUBSCRIPTION")
    public ApiResponseUtil.ApiResponseEntity<SubscriptionResponseDto> renewSubscription(String subscriptionId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, subscriptionService.renewSubscription(subscriptionId, userId), "Subscription renewed successfully", null);
    }

    // POST /api/v1/subscriptions/{id}/generate-job

    // GET /api/v1/subscriptions/{id}/invoices

    // GET /api/v1/subscriptions/{id}/jobs

    // GET /api/v1/subscriptions/{id}/invoices


}
