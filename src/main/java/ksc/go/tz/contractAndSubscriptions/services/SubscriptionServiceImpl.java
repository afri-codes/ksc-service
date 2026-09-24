package ksc.go.tz.contractAndSubscriptions.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.entities.Subscription;
import ksc.go.tz.contractAndSubscriptions.repository.ContractRepository;
import ksc.go.tz.contractAndSubscriptions.repository.SubscriptionRepository;
import ksc.go.tz.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final ContractRepository contractRepository;

    @Override
    public SubscriptionResponseDto addSubscription(SubscriptionResponseDto subscriptionDto, UUID createdBy) {
        Optional<Contract> contractOptional = contractRepository.findById(UUID.fromString(subscriptionDto.getContractId())); // Replace with actual contract retrieval logic
        if (contractOptional.isEmpty()) {
            throw new AfriException("Contract is required to create a subscription");
        }
        Subscription subscription = new Subscription();
        subscription.setContract(contractOptional.get());
        subscription.setStatus(SubscriptionStatus.valueOf(subscriptionDto.getStatus()));
        subscription.setRecurrence(subscriptionDto.getRecurrence());
        subscription.setNextRunDate(subscriptionDto.getNextRunDate());
        subscription.setCrewSize(subscriptionDto.getCrewSize());
        subscription.setCreatedBy(createdBy);
        return new SubscriptionResponseDto(subscriptionRepository.save(subscription));
    }

    @Override
    public List<SubscriptionResponseDto> getAll(UUID userId) {
        return subscriptionRepository.findAll().stream().map(SubscriptionResponseDto::new).toList();

    }

    @Override
    public SubscriptionResponseDto getSubscriptionById(String subscriptionId, UUID userId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(UUID.fromString(subscriptionId));
        if (subscriptionOptional.isEmpty()) {
            throw new AfriException("Subscription not found");
        }
        return new SubscriptionResponseDto(subscriptionOptional.get());
    }

    @Override
    public SubscriptionResponseDto updateSubscription(String subscriptionId, SubscriptionResponseDto subscriptionDto, UUID userId) {
        return null;
    }

    @Override
    public SubscriptionResponseDto cancelSubscription(String subscriptionId, UUID userId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(UUID.fromString(subscriptionId));
        if (subscriptionOptional.isEmpty()) {
            throw new AfriException("Subscription not found");
        }
        Subscription subscription = subscriptionOptional.get();
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        return new SubscriptionResponseDto(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponseDto resumeSubscription(String subscriptionId, UUID userId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(UUID.fromString(subscriptionId));
        if (subscriptionOptional.isEmpty()) {
            throw new AfriException("Subscription not found");
        }
        Subscription subscription = subscriptionOptional.get();
        subscription.setStatus(SubscriptionStatus.RESUMED);
        return new SubscriptionResponseDto(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponseDto pauseSubscription(String subscriptionId, UUID userId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(UUID.fromString(subscriptionId));
        if (subscriptionOptional.isEmpty()) {
            throw new AfriException("Subscription not found");
        }
        Subscription subscription = subscriptionOptional.get();
        subscription.setStatus(SubscriptionStatus.PAUSED);
        return new SubscriptionResponseDto(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponseDto renewSubscription(String subscriptionId, UUID userId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(UUID.fromString(subscriptionId));
        if (subscriptionOptional.isEmpty()) {
            throw new AfriException("Subscription not found");
        }
        Subscription subscription = subscriptionOptional.get();
        subscription.setStatus(SubscriptionStatus.RENEWED);
        return new SubscriptionResponseDto(subscriptionRepository.save(subscription));
    }
}
