package ksc.go.tz.contractAndSubscriptions.controllers;

import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;
import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractReasonDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractSignDto;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Contracts", description = "Service contracts created from accepted quotations: DRAFT → SENT → SIGNED → ACTIVE → EXPIRED, or REJECTED / TERMINATED")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    @Operation(summary = "Create a contract from an accepted quote", description = "Creates a DRAFT contract. The quote must be ACCEPTED and not already "
            + "have a contract. Service, site, frequency and value default from the quote; the quote's invoice is linked to the contract.")
    @Permission(name="SAVE NEW CONTRACT", code = "SAVE_CONTRACT")
    @PostMapping("/contracts")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> addContract(@RequestBody @Valid ContractDto contractDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, contractService.addContract(contractDto, createdBy), "Contract added successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Get contracts with pagination, sorting and filtering", description = "Filter by status (DRAFT, SENT, SIGNED, ACTIVE, EXPIRED, "
            + "TERMINATED, REJECTED), serviceLine (e.g. CLEANING) and clientId. sortBy: createdAt, startDate, endDate, contractValue, status, contractNumber.")
    @Permission(name = "VIEW ALL CONTRACTS", code = "VIEW_CONTRACTS")
    @GetMapping("/contracts/pagination")
    public ApiResponseUtil.ApiResponseEntity<Page<ContractResponseDto>> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceLine,
            @RequestParam(required = false) String clientId
    ) {
        Page<ContractResponseDto> contracts = contractService.getAllContractsWithPaginationAndSortingAndFiltering(
                page, size, sortBy, sortDir, status, serviceLine, clientId);
        return apiResponseUtil.getResponse(contracts);
    }

    @Operation(summary = "get all contract list ")
    @Permission(name="VIEW ALL CONTRACT", code = "VIEW_CONTRACT")
    @GetMapping("/contracts")
    public ApiResponseUtil.ApiResponseEntity<List<ContractResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(contractService.getAll(userId));
    }

    @Operation(summary = "Contracts expiring soon", description = "ACTIVE contracts whose end date is within the next `days` days (default 30, max 365), soonest first.")
    @Permission(name="VIEW EXPIRING CONTRACTS", code = "VIEW_EXPIRING_CONTRACTS")
    @GetMapping("/contracts/expiring")
    public ApiResponseUtil.ApiResponseEntity<List<ContractResponseDto>> getExpiring(@RequestParam(defaultValue = "30") int days) {
        return apiResponseUtil.getResponse(contractService.getExpiring(days));
    }

    @Operation(summary = "get contract by id ")
    @Permission(name="VIEW CONTRACT BY ID", code = "VIEW_CONTRACT_BY_ID")
    @GetMapping("/contracts/{id}")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> getById(@PathVariable("id") String contractId){
        return apiResponseUtil.getResponse(contractService.getContractById(contractId));
    }

    @Operation(summary = "Update contract", description = "Only DRAFT contracts can be edited.")
    @Permission(name="UPDATE CONTRACT", code = "UPDATE_CONTRACT")
    @PutMapping("/contracts/{id}")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> updateContract(@PathVariable("id") String contractId, @RequestBody @Valid ContractDto contractDto, Authentication authentication) {
        ContractResponseDto updatedContract = contractService.updateContract(contractId, contractDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedContract, "Contract updated successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Delete contract", description = "Soft delete. Only DRAFT or REJECTED contracts, and only by the user who created them.")
    @Permission(name="DELETE CONTRACT", code = "DELETE_CONTRACT")
    @DeleteMapping("/contracts/{id}")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> deleteContract(@PathVariable("id") String contractId, Authentication authentication) {
        ContractResponseDto deletedContract = contractService.deleteContract(contractId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, deletedContract, "Contract deleted successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Send contract", description = "DRAFT → SENT (sent to the client for signature).")
    @Permission(name="SEND CONTRACT", code = "SEND_CONTRACT")
    @PostMapping("/contracts/{id}/send")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> sendContract(@PathVariable("id") String contractId, Authentication authentication) {
        ContractResponseDto sentContract = contractService.sendContract(contractId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, sentContract, "Contract sent successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Reject contract", description = "SENT → REJECTED (the client declined). A reason is required.")
    @Permission(name="REJECT CONTRACT", code = "REJECT_CONTRACT")
    @PostMapping("/contracts/{id}/reject")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> rejectContract(@PathVariable("id") String contractId,
                                                                                 @RequestBody @Valid ContractReasonDto reason,
                                                                                 Authentication authentication) {
        ContractResponseDto rejectedContract = contractService.rejectContract(contractId, reason, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, rejectedContract, "Contract rejected successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Approve (sign) contract", description = "SENT → SIGNED: records who signed for the client. "
            + "If the start date has already arrived the contract becomes ACTIVE immediately; otherwise the daily job activates it on the start date.")
    @Permission(name="APPROVE CONTRACT", code = "APPROVE_CONTRACT")
    @PostMapping("/contracts/{id}/approve")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> approveContract(@PathVariable("id") String contractId,
                                                                                  @RequestBody @Valid ContractSignDto signature,
                                                                                  Authentication authentication) {
        ContractResponseDto approvedContract = contractService.approveContract(contractId, signature, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, approvedContract, "Contract approved successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Terminate contract", description = "SIGNED or ACTIVE → TERMINATED (ended early). A reason is required.")
    @Permission(name="TERMINATE CONTRACT", code = "TERMINATE_CONTRACT")
    @PostMapping("/contracts/{id}/terminate")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> terminateContract(@PathVariable("id") String contractId,
                                                                                    @RequestBody @Valid ContractReasonDto reason,
                                                                                    Authentication authentication) {
        ContractResponseDto terminated = contractService.terminateContract(contractId, reason, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, terminated, "Contract terminated successfully", ResponseEnum.SUCCESS);
    }

    @Operation(summary = "Download contract PDF", description = "Generates the contract document from its current data and returns it as a file download.")
    @Permission(name="DOWNLOAD CONTRACT PDF", code = "DOWNLOAD_CONTRACT_PDF")
    @GetMapping(value = "/contracts/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable("id") String contractId) {
        FileMetaData pdf = contractService.generatePdf(contractId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(pdf.getFileName()).build());
        headers.setContentLength(pdf.getBytes().length);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(new ByteArrayInputStream(pdf.getBytes())));
    }

    @Operation(summary = "Invoices for a contract", description = "Invoices linked to the contract (the quote's invoice is linked when the contract is created), newest first.")
    @Permission(name="VIEW CONTRACT INVOICES", code = "VIEW_CONTRACT_INVOICES")
    @GetMapping("/contracts/{id}/invoices")
    public ApiResponseUtil.ApiResponseEntity<List<InvoiceResponseDto>> getInvoices(@PathVariable("id") String contractId) {
        return apiResponseUtil.getResponse(contractService.getInvoices(contractId));
    }

    @Operation(summary = "Subscriptions for a contract", description = "Subscriptions under the contract, newest first (the contract itself is not repeated in each item).")
    @Permission(name="VIEW CONTRACT SUBSCRIPTIONS", code = "VIEW_CONTRACT_SUBSCRIPTIONS")
    @GetMapping("/contracts/{id}/subscriptions")
    public ApiResponseUtil.ApiResponseEntity<List<SubscriptionResponseDto>> getSubscriptions(@PathVariable("id") String contractId) {
        return apiResponseUtil.getResponse(contractService.getSubscriptions(contractId));
    }

    @Operation(summary = "Payments for a contract", description = "Payments (mobile money and cash) for any of the contract's invoices, newest first.")
    @Permission(name="VIEW CONTRACT PAYMENTS", code = "VIEW_CONTRACT_PAYMENTS")
    @GetMapping("/contracts/{id}/payments")
    public ApiResponseUtil.ApiResponseEntity<List<PaymentResponseDto>> getPayments(@PathVariable("id") String contractId) {
        return apiResponseUtil.getResponse(contractService.getPayments(contractId));
    }

}
