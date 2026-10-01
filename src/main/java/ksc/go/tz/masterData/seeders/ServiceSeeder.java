package ksc.go.tz.masterData.seeders;

import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.entities.Service;
import ksc.go.tz.masterData.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceSeeder implements CommandLineRunner {

    private static final List<String> SERVICE_NAMES = List.of(
            "Cleaning",
            "Fumigation",
            "Property Management"
    );

    private final ServiceRepository serviceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (String name : SERVICE_NAMES) {
            if (serviceRepository.existsByServiceNameIgnoreCase(name)) {
                continue;
            }
            Service service = new Service();
            service.setServiceName(name);
            service.setStatus(Status.ACTIVE);
            serviceRepository.save(service);
            log.info("Seeded service: {}", name);
        }
    }
}
