package ksc.go.tz.quotation.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import ksc.go.tz.masterData.services.PricingItemResolver;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.common.pdf.BillTo;
import ksc.go.tz.common.pdf.BillingDocument;
import ksc.go.tz.common.pdf.BillingPdfRenderer;
import afriUtils.responses.AfriException;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.common.ReferenceNumberGenerator;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.QuoteStatus;
import ksc.go.tz.quotation.dto.QuoteDto;
import ksc.go.tz.quotation.dto.QuoteResponseDto;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.quotation.repository.QuoteRepository;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import ksc.go.tz.sitesAndAssests.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final SiteRepository siteRepository;
    private final BillingPdfRenderer billingPdfRenderer;
    private final PricingItemResolver pricingItemResolver;

    /** Number of days an auto-generated quotation stays valid. */
    private static final int QUOTE_VALIDITY_DAYS = 30;

    @Override
    public QuoteResponseDto addSite(QuoteDto quoteDto, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Sites> siteOptional = siteRepository.findById(UUID.fromString(quoteDto.getSiteId()));
        if (siteOptional.isEmpty()) {
            throw new AfriException("Site not found");
        }
        Quote quote = new Quote();
        quote.setSite(siteOptional.get());
        quote.setServiceType(quoteDto.getServiceLine());
        quote.setClientTier(quoteDto.getClientTier());
        quote.setHourlyRate(quoteDto.getHourlyRate());
        quote.setEstimatedHours(quoteDto.getEstimatedHours());
        quote.setStatus(QuoteStatus.valueOf(quoteDto.getStatus()));
        quote.setAreaSqm(quoteDto.getAreaSqm());
        quote.setFrequency(quoteDto.getFrequency());
        quote.setValidUntil(quoteDto.getValidUntil());
        quote.setPriceMax(quoteDto.getPriceMax());
        quote.setPriceMin(quoteDto.getPriceMin());
        quote.setRequestedBy(createdBy.toString());
        quote.setCreatedBy(createdBy);
        quote.setCreatedAt(now);
        return new QuoteResponseDto(quoteRepository.save(quote));
    }

    @Override
    public Quote generateForSite(Sites site, UUID createdBy) {
        List<LineItem> items = pricingItemResolver.lineItems(site.getService(), site.getCleaningDepth(), site.getAddOns());

        Quote quote = new Quote();
        quote.setQuoteNumber(ReferenceNumberGenerator.next("QT"));
        quote.setSite(site);
        quote.setRequestedBy(createdBy.toString());
        quote.setServiceType(LeadServiceType.fromName(site.getService().getServiceName()));
        quote.setFrequency(Frequency.ONCE);
        quote.setAreaSqm(site.getAreaSqm());
        quote.setPriceMin(site.getTotalPrice());
        quote.setPriceMax(site.getTotalPrice());
        quote.setValidUntil(LocalDate.now().plusDays(QUOTE_VALIDITY_DAYS));
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setItems(items);
        quote.setCreatedBy(createdBy);
        quote.setCreatedAt(LocalDateTime.now());
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quote> findCurrentForSite(UUID siteId) {
        return quoteRepository.findBySiteIdOrderByCreatedAtDesc(siteId).stream()
                .filter(q -> q.getStatus() != QuoteStatus.EXPIRED && q.getStatus() != QuoteStatus.REJECTED)
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Quote> findCurrentForSites(Collection<UUID> siteIds) {
        Map<UUID, Quote> current = new HashMap<>();
        if (siteIds.isEmpty()) {
            return current;
        }
        // Newest first, so the first acceptable quote seen for a site is its current one.
        for (Quote quote : quoteRepository.findBySiteIdInOrderByCreatedAtDesc(siteIds)) {
            if (quote.getStatus() != QuoteStatus.EXPIRED && quote.getStatus() != QuoteStatus.REJECTED) {
                current.putIfAbsent(quote.getSite().getId(), quote);
            }
        }
        return current;
    }

    @Override
    public void expireOpenQuotes(UUID siteId, UUID userId) {
        for (Quote quote : quoteRepository.findBySiteIdOrderByCreatedAtDesc(siteId)) {
            if (quote.getStatus() == QuoteStatus.DRAFT || quote.getStatus() == QuoteStatus.SENT) {
                quote.setStatus(QuoteStatus.EXPIRED);
                quote.setUpdatedBy(userId);
                quote.setUpdatedAt(LocalDateTime.now());
                quoteRepository.save(quote);
            }
        }
    }

    @Override
    public QuoteResponseDto getQuoteById(String quoteId) {
        Optional<Quote> quoteOptional = quoteRepository.findById(UUID.fromString(quoteId));
        if(quoteOptional.isPresent()) {
            return new QuoteResponseDto(quoteOptional.get());
        } else {
            throw new AfriException("Quote not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetaData generatePdf(String quoteId) {
        UUID id;
        try {
            id = UUID.fromString(quoteId);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid quote ID: " + quoteId);
        }
        Quote quote = quoteRepository.findById(id).orElseThrow(() -> new AfriException("Quote not found"));

        String number = quote.getQuoteNumber() != null ? quote.getQuoteNumber() : "QT-" + quote.getId();
        List<LineItem> items = quote.getItems();
        List<String> notes = new ArrayList<>();
        if (items.isEmpty()) {
            // Manually created quotes carry a price range instead of line items.
            items = List.of(LineItem.of("Service: " + quote.getServiceType().getDisplayName(), quote.getPriceMax()));
            if (quote.getPriceMin() != null && quote.getPriceMin().compareTo(quote.getPriceMax()) != 0) {
                notes.add("Estimated price range: TZS " + quote.getPriceMin().toPlainString()
                        + " – " + quote.getPriceMax().toPlainString() + ". The total shows the upper estimate.");
            }
        }
        if (quote.getValidUntil() != null) {
            notes.add("This quotation is valid until " + BillingPdfRenderer.format(quote.getValidUntil()) + ".");
        }
        notes.add("All prices are in Tanzanian Shillings (TZS).");

        BillingDocument document = new BillingDocument(
                "QUOTATION",
                number,
                quote.getCreatedAt() != null ? quote.getCreatedAt().toLocalDate() : null,
                "Valid until",
                quote.getValidUntil(),
                quote.getStatus() != null ? quote.getStatus().name() : "-",
                BillTo.forSite(quote.getSite()),
                items,
                quote.getPriceMax(),
                notes
        );
        return new FileMetaData(billingPdfRenderer.render(document), "application/pdf", number + ".pdf");
    }

    @Override
    public QuoteResponseDto acceptQuote(String quoteId, UUID userId) {
        Optional<Quote> quoteOptional = quoteRepository.findById(UUID.fromString(quoteId));
        if(quoteOptional.isEmpty()) {
            throw new AfriException("Quote not found");
        }
        quoteOptional.ifPresent(quote -> {
            quote.setStatus(QuoteStatus.ACCEPTED);
            quote.setUpdatedAt(LocalDateTime.now());
            quoteRepository.save(quote);
        });
        return new QuoteResponseDto(quoteOptional.get());
    }

    @Override
    public QuoteResponseDto sendQuote(String quoteId, UUID userId) {
        Optional<Quote> quoteOptional = quoteRepository.findById(UUID.fromString(quoteId));
        if(quoteOptional.isEmpty()) {
            throw new AfriException("Quote not found");
        }
        quoteOptional.ifPresent(quote -> {
            quote.setStatus(QuoteStatus.SENT);
            quote.setUpdatedAt(LocalDateTime.now());
            quoteRepository.save(quote);
        });
        return new QuoteResponseDto(quoteOptional.get());
    }

    @Override
    public List<QuoteResponseDto> getAll(UUID userId) {
        return quoteRepository.findAll().stream().map(QuoteResponseDto::new).toList();

    }

    @Override
    public Optional<QuoteResponseDto> getById(String quoteId) {
        Optional<Quote> site = quoteRepository.findById(UUID.fromString(quoteId));
        if (site.isEmpty()) {
            throw new AfriException("Site not found");
        }
        return quoteRepository.findById(UUID.fromString(quoteId)).map(QuoteResponseDto::new);

    }

    @Override
    public QuoteResponseDto updateSite(String site, QuoteDto quoteDto, UUID userId) {

        UUID siteId;

        try {
            siteId = UUID.fromString(site);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid site ID: " + site);
        }

        Quote existingSite = quoteRepository.findById(siteId)
                .orElseThrow(() -> new AfriException("Site not found"));

        existingSite.setUpdatedBy(userId);
        existingSite.setServiceType(quoteDto.getServiceLine());
        existingSite.setClientTier(quoteDto.getClientTier());
        existingSite.setHourlyRate(quoteDto.getHourlyRate());
        existingSite.setEstimatedHours(quoteDto.getEstimatedHours());
        existingSite.setPriceMin(quoteDto.getPriceMin());
        existingSite.setPriceMax(quoteDto.getPriceMax());
        existingSite.setValidUntil(quoteDto.getValidUntil());
        existingSite.setFrequency(quoteDto.getFrequency());
        existingSite.setAreaSqm(quoteDto.getAreaSqm());
        existingSite.setUpdatedAt(LocalDateTime.now());

        Quote updatedSite = quoteRepository.save(existingSite);

        return new QuoteResponseDto(updatedSite);
    }

    @Override
    public QuoteResponseDto deleteById(String quoteId, UUID userId) {
        Optional<Quote> sites = quoteRepository.findById(UUID.fromString(quoteId));
        if(sites.isPresent()){
            Quote sites1 = sites.get();
            sites1.setDeletedAt(LocalDateTime.now());
            return new QuoteResponseDto(quoteRepository.save(sites1));
        } else {
            throw new AfriException("Site not found");
        }
    }



}
