package ksc.go.tz.masterData.services;

import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.masterData.entities.Service;
import ksc.go.tz.masterData.repository.ServiceRepository;
import ksc.go.tz.common.LineItem;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final ServiceRepository serviceRepository;

    public Service resolveService(String serviceId) {
        Service service = serviceRepository.findById(parseId(serviceId, "service"))
                .orElseThrow(() -> new AfriException("Service not found"));
        if (service.getStatus() != Status.ACTIVE) {
            throw new AfriException("Service '" + service.getServiceName() + "' is not active");
        }
        return service;
    }

    /** Cleaning services are priced by cleaning depth, so they must have one; other services may leave it out. */
    public boolean requiresCleaningDepth(Service service) {
        return LeadServiceType.fromName(service.getServiceName()).name().contains("CLEANING");
    }

    /**
     * The cleaning depth for a site of this service: required for cleaning services, optional (null when not
     * given) for others such as Fumigation and Property Management.
     */
    public CleaningDepth resolveCleaningDepthFor(Service service, String cleaningDepthId) {
        if (cleaningDepthId == null || cleaningDepthId.isBlank()) {
            if (requiresCleaningDepth(service)) {
                throw new AfriException("Cleaning depth must be provided for " + service.getServiceName() + " sites");
            }
            return null;
        }
        return resolveCleaningDepth(cleaningDepthId);
    }

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

    public BigDecimal totalPrice(Service service, CleaningDepth cleaningDepth, Collection<AddOn> addOns) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : lineItems(service, cleaningDepth, addOns)) {
            total = total.add(item.getAmount());
        }
        return total;
    }

    /**
     * Line items for a quotation/invoice: the service's fixed price (when it has one), then the cleaning depth
     * (when there is one), then each add-on sorted by name.
     */
    public List<LineItem> lineItems(Service service, CleaningDepth cleaningDepth, Collection<AddOn> addOns) {
        List<LineItem> items = new ArrayList<>();
        if (service != null && service.getPrice() != null && service.getPrice().signum() > 0) {
            items.add(LineItem.of("Service: " + service.getServiceName(), service.getPrice()));
        }
        if (cleaningDepth != null) {
            items.add(LineItem.of("Cleaning depth: " + cleaningDepth.getName(), cleaningDepth.getPrice()));
        }
        addOns.stream()
                .sorted(Comparator.comparing(AddOn::getName))
                .forEach(addOn -> items.add(LineItem.of("Add-on: " + addOn.getName(), addOn.getPrice())));
        return items;
    }

    private UUID parseId(String id, String label) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AfriException("Invalid " + label + " ID: " + id);
        }
    }
}
