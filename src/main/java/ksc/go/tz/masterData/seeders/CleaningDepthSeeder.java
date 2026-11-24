package ksc.go.tz.masterData.seeders;

import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.entities.CleaningDepth;
import ksc.go.tz.masterData.repository.CleaningDepthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleaningDepthSeeder implements CommandLineRunner {

    private record SeedItem(String name, String price, String description) {
    }

    private static final List<SeedItem> CLEANING_DEPTHS = List.of(
            new SeedItem("Shallow", "505", "Light surface cleaning: dusting, sweeping and mopping"),
            new SeedItem("Medium", "506", "Standard cleaning including kitchen and bathroom scrubbing"),
            new SeedItem("Deep", "507", "Thorough top-to-bottom cleaning including hard-to-reach areas")
    );

    private final CleaningDepthRepository cleaningDepthRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (SeedItem item : CLEANING_DEPTHS) {
            if (cleaningDepthRepository.existsByNameIgnoreCase(item.name())) {
                continue;
            }
            CleaningDepth cleaningDepth = new CleaningDepth();
            cleaningDepth.setName(item.name());
            cleaningDepth.setPrice(new BigDecimal(item.price()));
            cleaningDepth.setDescription(item.description());
            cleaningDepth.setStatus(Status.ACTIVE);
            cleaningDepthRepository.save(cleaningDepth);
            log.info("Seeded cleaning depth: {}", item.name());
        }
    }
}
