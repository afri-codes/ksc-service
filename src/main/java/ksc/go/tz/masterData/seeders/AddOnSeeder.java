package ksc.go.tz.masterData.seeders;

import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.entities.AddOn;
import ksc.go.tz.masterData.repository.AddOnRepository;
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
public class AddOnSeeder implements CommandLineRunner {

    private record SeedItem(String name, String price, String description) {
    }

    private static final List<SeedItem> ADD_ONS = List.of(
            new SeedItem("Window Cleaning", "500", "Interior and exterior window and glass cleaning"),
            new SeedItem("Carpet Cleaning", "501", "Shampoo and vacuum cleaning of carpets and rugs"),
            new SeedItem("Sofa Cleaning", "502", "Upholstery cleaning for sofas and chairs"),
            new SeedItem("Fridge Cleaning", "503", "Inside and outside cleaning of the refrigerator"),
            new SeedItem("Oven Cleaning", "504", "Degreasing and cleaning of oven and cooker")
    );

    private final AddOnRepository addOnRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (SeedItem item : ADD_ONS) {
            if (addOnRepository.existsByNameIgnoreCase(item.name())) {
                continue;
            }
            AddOn addOn = new AddOn();
            addOn.setName(item.name());
            addOn.setPrice(new BigDecimal(item.price()));
            addOn.setDescription(item.description());
            addOn.setStatus(Status.ACTIVE);
            addOnRepository.save(addOn);
            log.info("Seeded add-on: {}", item.name());
        }
    }
}
