package ksc.go.tz.masterData.seeders;

import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.entities.Service;
import ksc.go.tz.masterData.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceSeeder implements CommandLineRunner {

    private record SeedItem(String name, BigDecimal price) {
    }

    // Fixed prices in TZS (placeholders). Cleaning has none: it is priced by cleaning depth.
    private static final List<SeedItem> SERVICES = List.of(
            new SeedItem("Cleaning", null),
            new SeedItem("Fumigation", new BigDecimal("500.00")),
            new SeedItem("Property Management", new BigDecimal("501.00"))
    );

    private final ServiceRepository serviceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (SeedItem item : SERVICES) {
            Optional<Service> existing = serviceRepository.findByServiceNameIgnoreCase(item.name());
            if (existing.isPresent()) {
                // Fill in a missing price once; never overwrite a price set through the API.
                Service service = existing.get();
                if (service.getPrice() == null && item.price() != null) {
                    service.setPrice(item.price());
                    serviceRepository.save(service);
                    log.info("Set seed price for service: {}", item.name());
                }
                continue;
            }
            Service service = new Service();
            service.setServiceName(item.name());
            service.setPrice(item.price());
            service.setStatus(Status.ACTIVE);
            serviceRepository.save(service);
            log.info("Seeded service: {}", item.name());
        }
    }
}
