package ksc.go.tz.masterData.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.entities.AddOn;
import ksc.go.tz.masterData.entities.CleaningDepth;
import ksc.go.tz.masterData.entities.Service;
import ksc.go.tz.masterData.repository.AddOnRepository;
import ksc.go.tz.masterData.repository.CleaningDepthRepository;
import ksc.go.tz.masterData.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingItemResolverTest {

    private CleaningDepthRepository cleaningDepthRepository;
    private PricingItemResolver resolver;

    @BeforeEach
    void setUp() {
        cleaningDepthRepository = mock(CleaningDepthRepository.class);
        resolver = new PricingItemResolver(cleaningDepthRepository, mock(AddOnRepository.class), mock(ServiceRepository.class));
    }

    @Test
    void onlyCleaningServicesRequireACleaningDepth() {
        assertTrue(resolver.requiresCleaningDepth(service("Cleaning")));
        assertFalse(resolver.requiresCleaningDepth(service("Fumigation")));
        assertFalse(resolver.requiresCleaningDepth(service("Property Management")));
    }

    @Test
    void cleaningSiteWithoutCleaningDepthIsRejected() {
        AfriException e = assertThrows(AfriException.class,
                () -> resolver.resolveCleaningDepthFor(service("Cleaning"), " "));
        assertTrue(e.getMessage().contains("Cleaning depth must be provided for Cleaning sites"));
    }

    @Test
    void fumigationAndPropertyManagementMayOmitCleaningDepth() {
        assertNull(resolver.resolveCleaningDepthFor(service("Fumigation"), null));
        assertNull(resolver.resolveCleaningDepthFor(service("Property Management"), ""));
    }

    @Test
    void fumigationMayStillChooseACleaningDepth() {
        CleaningDepth deep = cleaningDepth("Deep", "120000");
        when(cleaningDepthRepository.findById(deep.getId())).thenReturn(Optional.of(deep));

        assertSame(deep, resolver.resolveCleaningDepthFor(service("Fumigation"), deep.getId().toString()));
    }

    @Test
    void pricingWithoutCleaningDepthUsesAddOnsOnly() {
        Set<AddOn> addOns = Set.of(addOn("Window Cleaning", "20000"), addOn("Carpet Cleaning", "35000"));

        assertEquals(0, new BigDecimal("55000").compareTo(resolver.totalPrice(service("Fumigation"), null, addOns)));
        List<LineItem> items = resolver.lineItems(service("Fumigation"), null, addOns);
        assertEquals(List.of("Add-on: Carpet Cleaning", "Add-on: Window Cleaning"),
                items.stream().map(LineItem::getDescription).toList());
    }

    @Test
    void pricingWithNothingSelectedIsZeroWithNoItems() {
        assertEquals(0, BigDecimal.ZERO.compareTo(resolver.totalPrice(service("Fumigation"), null, Set.of())));
        assertTrue(resolver.lineItems(service("Fumigation"), null, Set.of()).isEmpty());
    }

    @Test
    void cleaningDepthComesFirstWhenPresent() {
        List<LineItem> items = resolver.lineItems(service("Cleaning"), cleaningDepth("Deep", "120000"), Set.of(addOn("Window Cleaning", "20000")));

        assertEquals("Cleaning depth: Deep", items.get(0).getDescription());
        assertEquals(0, new BigDecimal("140000").compareTo(
                resolver.totalPrice(service("Cleaning"), cleaningDepth("Deep", "120000"), Set.of(addOn("Window Cleaning", "20000")))));
    }

    @Test
    void servicePriceIsTheFirstLineAndAddsToTheTotal() {
        Service fumigation = service("Fumigation", "150000");
        List<LineItem> items = resolver.lineItems(fumigation, null, Set.of(addOn("Window Cleaning", "20000")));

        assertEquals(List.of("Service: Fumigation", "Add-on: Window Cleaning"),
                items.stream().map(LineItem::getDescription).toList());
        assertEquals(0, new BigDecimal("170000").compareTo(
                resolver.totalPrice(fumigation, null, Set.of(addOn("Window Cleaning", "20000")))));
    }

    @Test
    void servicePriceAloneIsEnoughToPriceASite() {
        Service propertyManagement = service("Property Management", "200000");

        assertEquals(1, resolver.lineItems(propertyManagement, null, Set.of()).size());
        assertEquals(0, new BigDecimal("200000").compareTo(resolver.totalPrice(propertyManagement, null, Set.of())));
    }

    @Test
    void serviceWithoutPriceOrWithZeroPriceAddsNoLine() {
        assertTrue(resolver.lineItems(service("Cleaning"), null, Set.of()).isEmpty());
        assertTrue(resolver.lineItems(service("Fumigation", "0"), null, Set.of()).isEmpty());
    }

    private static Service service(String name, String price) {
        Service service = service(name);
        service.setPrice(new BigDecimal(price));
        return service;
    }

    private static Service service(String name) {
        Service service = new Service();
        service.setId(UUID.randomUUID());
        service.setServiceName(name);
        service.setStatus(Status.ACTIVE);
        return service;
    }

    private static CleaningDepth cleaningDepth(String name, String price) {
        CleaningDepth depth = new CleaningDepth();
        depth.setId(UUID.randomUUID());
        depth.setName(name);
        depth.setPrice(new BigDecimal(price));
        depth.setStatus(Status.ACTIVE);
        return depth;
    }

    private static AddOn addOn(String name, String price) {
        AddOn addOn = new AddOn();
        addOn.setId(UUID.randomUUID());
        addOn.setName(name);
        addOn.setPrice(new BigDecimal(price));
        addOn.setStatus(Status.ACTIVE);
        return addOn;
    }
}
