package ksc.go.tz.billing.controllers;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.billing.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "Invoices", description = "Invoices generated from quotations")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    @Operation(summary = "get all invoice list ")
    @Permission(name="VIEW ALL INVOICE", code = "VIEW_INVOICE")
    @GetMapping("/invoices")
    public ApiResponseUtil.ApiResponseEntity<List<InvoiceResponseDto>> getAll(Authentication authentication) {
        return apiResponseUtil.getResponse(invoiceService.getAll(authDetailsExtractor.getUserId(authentication)));
    }

    @Operation(summary = "get invoice by id ")
    @Permission(name="VIEW INVOICE BY ID", code = "VIEW_INVOICE_BY_ID")
    @GetMapping("/invoices/{id}")
    public ApiResponseUtil.ApiResponseEntity<InvoiceResponseDto> getById(@PathVariable("id") String invoiceId) {
        Optional<InvoiceResponseDto> invoice = invoiceService.getById(invoiceId);
        if (invoice.isEmpty()) {
            return apiResponseUtil.getResponse(null, null, "Invoice not found", ResponseEnum.NOT_FOUND);
        }
        return apiResponseUtil.getResponse(invoice.get());
    }

    @Operation(summary = "get invoices for a site ")
    @Permission(name="VIEW SITE INVOICES", code = "VIEW_SITE_INVOICES")
    @GetMapping("/sites/{siteId}/invoices")
    public ApiResponseUtil.ApiResponseEntity<List<InvoiceResponseDto>> getBySiteId(@PathVariable("siteId") String siteId) {
        return apiResponseUtil.getResponse(invoiceService.getBySiteId(siteId));
    }

    // POST /api/v1/invoices


    // PUT /api/v1/invoices/{id}

    // POST /api/v1/invoices/{id}/cancel

    // POST /api/v1/invoices/{id}/send


    @Operation(summary = "download invoice PDF ", description = "Generates the invoice as a PDF from its current data and returns it as a file download.")
    @Permission(name="DOWNLOAD INVOICE PDF", code = "DOWNLOAD_INVOICE_PDF")
    @GetMapping(value = "/invoices/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable("id") String invoiceId) {
        FileMetaData pdf = invoiceService.generatePdf(invoiceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(pdf.getFileName()).build());
        headers.setContentLength(pdf.getBytes().length);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(new ByteArrayInputStream(pdf.getBytes())));
    }


}
