package ksc.go.tz.quoting.services;



import ksc.go.tz.quoting.dto.QuoteDto;
import ksc.go.tz.quoting.dto.QuoteResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteService {

    List<QuoteResponseDto> getAll(UUID userId);

    QuoteResponseDto deleteById(String quoteId, UUID userId);

    Optional<QuoteResponseDto> getById(String quoteId);

    QuoteResponseDto updateSite(String site, QuoteDto quoteDto, UUID userId);

    QuoteResponseDto addSite(QuoteDto quoteDto, UUID createdBy);
}
