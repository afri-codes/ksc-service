package ksc.go.tz.quotation.controllers;

import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayInputStream;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.quotation.dto.QuoteDto;
import ksc.go.tz.quotation.dto.QuoteResponseDto;
import ksc.go.tz.quotation.services.QuoteService;
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

    @Operation(summary = "get quote by id ")
    @Permission(name="VIEW QUOTE BY ID", code = "VIEW_QUOTE_BY_ID")
    @GetMapping("/quotes/{id}")
    public ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> getById(@PathVariable("id") String quoteId){
        QuoteResponseDto quote = quoteService.getQuoteById(quoteId);
        if (quote == null) {
            return apiResponseUtil.getResponse(null, null, "Quote not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(quote);

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
    @DeleteMapping("/quotes/{id}")
    public ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> deleteById(@PathVariable("id") String quoteId, Authentication authentication){
        QuoteResponseDto quoteResponseDto =  quoteService.deleteById(quoteId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, quoteResponseDto,"Quote deleted successful",ResponseEnum.SUCCESS);

    }


    @Operation(summary = "download quotation PDF ", description = "Generates the quotation as a PDF from its current data and returns it as a file download.")
    @Permission(name="DOWNLOAD QUOTE PDF", code = "DOWNLOAD_QUOTE_PDF")
    @GetMapping(value = "/quotes/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable("id") String quoteId) {
        FileMetaData pdf = quoteService.generatePdf(quoteId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(pdf.getFileName()).build());
        headers.setContentLength(pdf.getBytes().length);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(new ByteArrayInputStream(pdf.getBytes())));
    }

    @Operation(summary = "Send quote")
    @Permission(name="SEND QUOTE", code = "SEND_QUOTE")
    @PostMapping("/quotes/{id}/send")
    public ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> sendQuote(@PathVariable("id") String quoteId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        QuoteResponseDto sentQuote = quoteService.sendQuote(quoteId, userId);
        return apiResponseUtil.getResponse(null, sentQuote, "Quote sent successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Accept quote")
    @Permission(name="ACCEPT QUOTE", code = "ACCEPT_QUOTE")
    @PostMapping("/quotes/{id}/accept")
    public ApiResponseUtil.ApiResponseEntity<QuoteResponseDto> acceptQuote(@PathVariable("id") String quoteId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        QuoteResponseDto acceptedQuote = quoteService.acceptQuote(quoteId, userId);
        return apiResponseUtil.getResponse(null, acceptedQuote, "Quote accepted successfully", ResponseEnum.SUCCESS);
    }

}
