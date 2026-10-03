package ksc.go.tz.quotation.services;



import java.util.Collection;
import java.util.Map;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import ksc.go.tz.quotation.dto.QuoteDto;
import ksc.go.tz.quotation.dto.QuoteResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteService {

    List<QuoteResponseDto> getAll(UUID userId);

    QuoteResponseDto deleteById(String quoteId, UUID userId);

    Optional<QuoteResponseDto> getById(String quoteId);

    QuoteResponseDto updateSite(String site, QuoteDto quoteDto, UUID userId);

    QuoteResponseDto addSite(QuoteDto quoteDto, UUID createdBy);

    QuoteResponseDto acceptQuote(String quoteId, UUID userId);

    QuoteResponseDto sendQuote(String quoteId, UUID userId);

    Quote generateForSite(Sites site, UUID createdBy);

    FileMetaData generatePdf(String quoteId);

    /** The site's latest quote that has not been expired or rejected, if any. */
    Optional<Quote> findCurrentForSite(UUID siteId);

    /** Current quote (latest not expired or rejected) per site, for the given sites; sites without one are absent. */
    Map<UUID, Quote> findCurrentForSites(Collection<UUID> siteIds);

    /** Marks the site's DRAFT and SENT quotes as EXPIRED. */
    void expireOpenQuotes(UUID siteId, UUID userId);

    QuoteResponseDto getQuoteById(String quoteId);
}
