package ksc.go.tz.contractAndSubscriptions.services;


import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {


    SubscriptionResponseDto addSubscription(SubscriptionResponseDto subscriptionDto, UUID createdBy);

    List<SubscriptionResponseDto> getAll(UUID userId);

    SubscriptionResponseDto getSubscriptionById(String subscriptionId, UUID userId);

    SubscriptionResponseDto updateSubscription(String subscriptionId, SubscriptionResponseDto subscriptionDto, UUID userId);

    SubscriptionResponseDto cancelSubscription(String subscriptionId, UUID userId);

    SubscriptionResponseDto resumeSubscription(String subscriptionId, UUID userId);

    SubscriptionResponseDto pauseSubscription(String subscriptionId, UUID userId);

    SubscriptionResponseDto renewSubscription(String subscriptionId, UUID userId);
}
