package ksc.go.tz.quoting.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.QuoteStatus;
import ksc.go.tz.quoting.dto.QuoteDto;
import ksc.go.tz.quoting.dto.QuoteResponseDto;
import ksc.go.tz.quoting.entities.Quote;
import ksc.go.tz.quoting.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;

    @Override
    public QuoteResponseDto addSite(QuoteDto quoteDto, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Quote sites = new Quote();

        sites.setCreatedBy(createdBy);
        sites.setCreatedAt(now);
        return new QuoteResponseDto(quoteRepository.save(sites));
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
