package ksc.go.tz.quoting.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.quoting.dto.QuoteDto;
import ksc.go.tz.quoting.dto.QuoteResponseDto;
import ksc.go.tz.quoting.services.QuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService quoteService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;


    @Operation(summary = "Save or add new quote ")
    @Permission(name="SAVE NEW QUOTE", code = "SAVE_QUOTE")
    @PostMapping("/quotes")
    public  ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> saveSite(@RequestBody @Valid QuoteDto quoteDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, quoteService.addSite(quoteDto, createdBy), "Quote added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "get all quote list ")
    @Permission(name="VIEW ALL QUOTE", code = "VIEW_QUOTE")
    @GetMapping("/quotes")
    public ApiResponseUtil.ApiResponseEntity<List<QuoteResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(quoteService.getAll(userId));

    }

    @Operation(summary = "update quote ")
    @Permission(name="UPDATE QUOTE", code = "UPDATE_QUOTE")
    @PutMapping("/quotes/{id}")
    public ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> updateSite(@PathVariable("id") String siteId, @RequestBody QuoteDto siteDto, Authentication authentication) {
        QuoteResponseDto updatedSite = quoteService.updateSite(siteId, siteDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedSite,"Quote updated successful",ResponseEnum.SUCCESS);

    }
    @Operation(summary = "soft delete quote ")
    @Permission(name="DELETE QUOTE", code = "DELETE_QUOTE")
    @DeleteMapping("/quotes/{quoteId}")
    public ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> deleteById(@PathVariable (name = "id") String siteId, Authentication authentication){
        QuoteResponseDto quoteResponseDto =  quoteService.deleteById(siteId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, quoteResponseDto,"Quote deleted successful",ResponseEnum.SUCCESS);

    }


    // POST /api/v1/quotes/{id}/send

    // POST /api/v1/quotes/{id}/accept

    // GET /api/v1/quotes/{id}/pdf

    // POST /api/v1/quotes/{id}/email

    // POST /api/v1/quotes/{id}/create-contract


}
