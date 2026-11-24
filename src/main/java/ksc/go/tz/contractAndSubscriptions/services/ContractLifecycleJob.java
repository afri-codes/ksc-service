package ksc.go.tz.contractAndSubscriptions.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Moves contracts along by date: SIGNED → ACTIVE on the start date, ACTIVE → EXPIRED after the end date.
 * Runs daily (default 00:15) and once shortly after startup, so a restart never leaves contracts behind.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractLifecycleJob {

    private final ContractService contractService;

    @Scheduled(cron = "${ksc.contract.lifecycle-cron:0 15 0 * * *}")
    public void daily() {
        run();
    }

    @Scheduled(initialDelayString = "${ksc.contract.lifecycle-initial-delay-ms:60000}", fixedDelay = Long.MAX_VALUE)
    public void afterStartup() {
        run();
    }

    private void run() {
        int changed = contractService.applyDateTransitions();
        if (changed > 0) {
            log.info("[CONTRACT] Lifecycle job updated {} contract(s)", changed);
        }
    }
}
