package ksc.go.tz.sitesAndAssests.services;

import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.quotation.dto.QuoteResponseDto;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.enums.QuoteStatus;
import java.math.BigDecimal;
import afriUtils.responses.AfriException;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.billing.services.InvoiceService;
import ksc.go.tz.billing.services.PaymentService;
import ksc.go.tz.masterData.entities.AddOn;
import ksc.go.tz.masterData.entities.CleaningDepth;
import ksc.go.tz.masterData.services.PricingItemResolver;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.quotation.services.QuoteService;
import ksc.go.tz.sitesAndAssests.dto.SiteDto;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import ksc.go.tz.sitesAndAssests.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final PricingItemResolver pricingItemResolver;
    private final QuoteService quoteService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @Override
    public SiteResponseDto addSite(SiteDto siteDto, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Sites sites = new Sites();
        sites.setSiteType(siteDto.getSiteType());
        sites.setAreaSqm(siteDto.getAreaSqm());
        sites.setRoomCount(siteDto.getRoomCount());
        sites.setAddressArea(siteDto.getAddressArea());
        sites.setLatitude(siteDto.getLatitude());
        sites.setLongitude(siteDto.getLongitude());
        Map<String, Object> plotCoordinates = new HashMap<>();
        plotCoordinates.put("latitude", Double.valueOf(siteDto.getLatitude()));
        plotCoordinates.put("longitude", Double.valueOf(siteDto.getLongitude()));
        sites.setPlotCoordinates(plotCoordinates);
        sites.setSecured(siteDto.getSecured());
        sites.setAccessType(siteDto.getAccessType());
        sites.setService(pricingItemResolver.resolveService(siteDto.getServiceId()));
        applyPricing(sites, siteDto, sites.getService());
        sites.setCreatedBy(createdBy);
        sites.setSite_owner(String.valueOf(createdBy.toString()));
        sites.setCreatedAt(now);
        Sites savedSite = siteRepository.save(sites);

        // Every new site with something to charge for gets a draft quotation and a pending invoice.
        SiteResponseDto response = new SiteResponseDto(savedSite);
        generateQuoteAndInvoice(savedSite, createdBy, response);
        return response;
    }

    @Override
    public List<SiteResponseDto> getAll(UUID userId) {
        return withCurrentDocuments(siteRepository.findAll(), false);
    }

    @Override
    public Optional<SiteResponseDto> getById(String siteId) {
        Optional<Sites> site = siteRepository.findById(UUID.fromString(siteId));
        if (site.isEmpty()) {
            throw new AfriException("Site not found");
        }
        SiteResponseDto response = withCurrentDocuments(List.of(site.get()), true).get(0);
        response.setPayments(paymentService.getBySiteId(site.get().getId()));
        return Optional.of(response);

    }

    @Override
    public SiteResponseDto updateSite(String site, SiteDto siteDto, UUID userId) {

        UUID siteId;

        try {
            siteId = UUID.fromString(site);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid site ID: " + site);
        }

        Sites existingSite = siteRepository.findById(siteId)
                .orElseThrow(() -> new AfriException("Site not found"));

        // Work out whether this update changes what the site is quoted for, before touching anything.
        ksc.go.tz.masterData.entities.Service service = pricingItemResolver.resolveService(siteDto.getServiceId());
        CleaningDepth cleaningDepth = pricingItemResolver.resolveCleaningDepthFor(service, siteDto.getCleaningDepthId());
        Set<AddOn> addOns = pricingItemResolver.resolveAddOns(siteDto.getAddOnIds());
        List<LineItem> newItems = pricingItemResolver.lineItems(service, cleaningDepth, addOns);
        BigDecimal newTotal = pricingItemResolver.totalPrice(service, cleaningDepth, addOns);
        Optional<Quote> currentQuote = quoteService.findCurrentForSite(siteId);
        boolean pricingChanged = currentQuote
                .map(q -> !samePricing(q, newItems, newTotal)
                        || q.getServiceType() != LeadServiceType.fromName(service.getServiceName()))
                .orElse(true);
        if (pricingChanged) {
            assertPricingNotLocked(siteId, currentQuote);
        }

        existingSite.setSiteType(siteDto.getSiteType());
        existingSite.setAreaSqm(siteDto.getAreaSqm());
        existingSite.setRoomCount(siteDto.getRoomCount());
        existingSite.setAddressArea(siteDto.getAddressArea());
        existingSite.setLatitude(siteDto.getLatitude());
        existingSite.setLongitude(siteDto.getLongitude());
        existingSite.setSecured(siteDto.getSecured());
        existingSite.setAccessType(siteDto.getAccessType());
        existingSite.setService(service);
        existingSite.setCleaningDepth(cleaningDepth);
        existingSite.setAddOns(addOns);
        existingSite.setTotalPrice(newTotal);
        existingSite.setUpdatedBy(userId);
        existingSite.setUpdatedAt(LocalDateTime.now());

        Sites updatedSite = siteRepository.save(existingSite);
        SiteResponseDto response = new SiteResponseDto(updatedSite);

        if (pricingChanged) {
            // Supersede the old open documents rather than changing them under the same number.
            quoteService.expireOpenQuotes(siteId, userId);
            invoiceService.cancelPendingInvoices(siteId, userId);
            generateQuoteAndInvoice(updatedSite, userId, response);
        }
        return response;
    }

    @Override
    public SiteResponseDto deleteById(String containerTypeId, UUID userId) {
        Optional<Sites> sites = siteRepository.findById(UUID.fromString(containerTypeId));
        if(sites.isPresent()){
            Sites sites1 = sites.get();
            sites1.setDeletedAt(LocalDateTime.now());
            return new SiteResponseDto(siteRepository.save(sites1));
        } else {
            throw new AfriException("Site not found");
        }
    }

    /**
     * Site responses with each site's current quotation and invoice filled in (two queries for the whole list).
     * With {@code includeDetails}, the full quote and invoice are attached as well.
     */
    private List<SiteResponseDto> withCurrentDocuments(List<Sites> sites, boolean includeDetails) {
        List<UUID> siteIds = sites.stream().map(Sites::getId).toList();
        Map<UUID, Quote> quotes = quoteService.findCurrentForSites(siteIds);
        Map<UUID, Invoice> invoices = invoiceService.findCurrentForSites(siteIds);
        return sites.stream().map(site -> {
            SiteResponseDto response = new SiteResponseDto(site);
            Quote quote = quotes.get(site.getId());
            if (quote != null) {
                response.setQuoteId(quote.getId().toString());
                response.setQuoteNumber(quote.getQuoteNumber());
                if (includeDetails) {
                    response.setQuote(new QuoteResponseDto(quote, false));
                }
            }
            Invoice invoice = invoices.get(site.getId());
            if (invoice != null) {
                response.setInvoiceId(invoice.getId().toString());
                response.setInvoiceNumber(invoice.getInvoiceNumber());
                if (includeDetails) {
                    response.setInvoice(new InvoiceResponseDto(invoice));
                }
            }
            return response;
        }).toList();
    }

    private void generateQuoteAndInvoice(Sites site, UUID userId, SiteResponseDto response) {
        if (pricingItemResolver.lineItems(site.getService(), site.getCleaningDepth(), site.getAddOns()).isEmpty()) {
            // Nothing priced (no service price, cleaning depth or add-ons): a TZS 0 quote and invoice would be noise.
            log.info("[SITE] Site {} has nothing priced; no quotation or invoice generated", site.getId());
            return;
        }
        Quote quote = quoteService.generateForSite(site, userId);
        Invoice invoice = invoiceService.generateForQuote(quote, userId);
        response.setQuoteId(quote.getId().toString());
        response.setQuoteNumber(quote.getQuoteNumber());
        response.setInvoiceId(invoice.getId().toString());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
    }

    /** True when the quote already charges exactly these items (or, for a quote without items, this total). */
    private boolean samePricing(Quote quote, List<LineItem> items, BigDecimal total) {
        List<LineItem> current = quote.getItems();
        if (current.isEmpty()) {
            return quote.getPriceMax() != null && quote.getPriceMax().compareTo(total) == 0;
        }
        if (current.size() != items.size()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem a = current.get(i);
            LineItem b = items.get(i);
            if (!a.getDescription().equals(b.getDescription()) || a.getAmount().compareTo(b.getAmount()) != 0) {
                return false;
            }
        }
        return true;
    }

    private void assertPricingNotLocked(UUID siteId, Optional<Quote> currentQuote) {
        if (currentQuote.isPresent() && currentQuote.get().getStatus() == QuoteStatus.ACCEPTED) {
            throw new AfriException("Quotation " + currentQuote.get().getQuoteNumber()
                    + " for this site has been accepted, so its cleaning depth and add-ons can no longer be changed.");
        }
        if (paymentService.hasPaymentInProgressForSite(siteId)) {
            throw new AfriException("A payment for this site's invoice is in progress, so its cleaning depth and add-ons "
                    + "can't be changed until the payment service reports the result.");
        }
        invoiceService.findSettledForSite(siteId).ifPresent(invoice -> {
            throw new AfriException("Invoice " + invoice.getInvoiceNumber() + " for this site is " + invoice.getStatus()
                    + ", so its cleaning depth and add-ons can no longer be changed.");
        });
    }

    private void applyPricing(Sites site, SiteDto siteDto, ksc.go.tz.masterData.entities.Service service) {
        CleaningDepth cleaningDepth = pricingItemResolver.resolveCleaningDepthFor(service, siteDto.getCleaningDepthId());
        Set<AddOn> addOns = pricingItemResolver.resolveAddOns(siteDto.getAddOnIds());
        site.setCleaningDepth(cleaningDepth);
        site.setAddOns(addOns);
        site.setTotalPrice(pricingItemResolver.totalPrice(service, cleaningDepth, addOns));
    }

}
