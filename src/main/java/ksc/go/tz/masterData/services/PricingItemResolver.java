package ksc.go.tz.masterData.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.entities.AddOn;
import ksc.go.tz.masterData.entities.CleaningDepth;
import ksc.go.tz.masterData.repository.AddOnRepository;
import ksc.go.tz.masterData.repository.CleaningDepthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PricingItemResolver {

    private final CleaningDepthRepository cleaningDepthRepository;
    private final AddOnRepository addOnRepository;

    public CleaningDepth resolveCleaningDepth(String cleaningDepthId) {
        CleaningDepth cleaningDepth = cleaningDepthRepository.findById(parseId(cleaningDepthId, "cleaning depth"))
                .orElseThrow(() -> new AfriException("Cleaning depth not found"));
        if (cleaningDepth.getStatus() != Status.ACTIVE) {
            throw new AfriException("Cleaning depth '" + cleaningDepth.getName() + "' is not active");
        }
        return cleaningDepth;
    }

    public Set<AddOn> resolveAddOns(List<String> addOnIds) {
        if (addOnIds == null || addOnIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<UUID> ids = new HashSet<>();
        for (String id : addOnIds) {
            ids.add(parseId(id, "add-on"));
        }
        List<AddOn> addOns = addOnRepository.findAllById(ids);
        if (addOns.size() != ids.size()) {
            throw new AfriException("One or more add-ons not found");
        }
        for (AddOn addOn : addOns) {
            if (addOn.getStatus() != Status.ACTIVE) {
                throw new AfriException("Add-on '" + addOn.getName() + "' is not active");
            }
        }
        return new HashSet<>(addOns);
    }

    public BigDecimal totalPrice(CleaningDepth cleaningDepth, Collection<AddOn> addOns) {
        BigDecimal total = cleaningDepth.getPrice();
        for (AddOn addOn : addOns) {
            total = total.add(addOn.getPrice());
        }
        return total;
    }

    private UUID parseId(String id, String label) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AfriException("Invalid " + label + " ID: " + id);
        }
    }
}
