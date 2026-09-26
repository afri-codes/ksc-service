package ksc.go.tz.contractAndSubscriptions.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

    // POST /api/v1/contracts
    @Operation(summary = "Save or add new contract")
    @Permission(name="SAVE NEW CONTRACT", code = "SAVE_CONTRACT")
    @PostMapping("/contracts")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> addContract(@RequestBody @Valid ContractDto contractDto, Authentication authentication) {
        UUID createdBy = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(null, contractService.addContract(contractDto, createdBy), "Contract added successfully", null);
    }

    @Operation(summary = "Get all contracts with pagination, sorting and filtering by status, source and service line")
    @Permission(name = "VIEW ALL CONTRACTS", code = "VIEW_CONTRACTS")
    @GetMapping("/contracts/pagination")
    public ApiResponseUtil.ApiResponseEntity<Page<ContractResponseDto>> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String serviceLine,
            Authentication authentication
    ) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        Page<ContractResponseDto> contracts = contractService.getAllContractsWithPaginationAndSortingAndFiltering(
                page, size, sortBy, sortDir, serviceLine, userId);
        return apiResponseUtil.getResponse(contracts);

    }


    @Operation(summary = "get all contract list ")
    @Permission(name="VIEW ALL CONTRACT", code = "VIEW_CONTRACT")
    @GetMapping("/contracts")
    public ApiResponseUtil.ApiResponseEntity<List<ContractResponseDto>> getAll(Authentication authentication){
        UUID userId = authDetailsExtractor.getUserId(authentication);
        return apiResponseUtil.getResponse(contractService.getAll(userId));

    }

    // PUT /api/v1/contracts/{id}
    @Operation(summary = "Update contract")
    @Permission(name="UPDATE CONTRACT", code = "UPDATE_CONTRACT")
    @PutMapping("/contracts/{id}")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> updateContract(@PathVariable("id") String contractId, @RequestBody ContractDto contractDto, Authentication authentication) {
        ContractResponseDto updatedContract = contractService.updateContract(contractId, contractDto, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, updatedContract, "Contract updated successfully", null);
    }

    // DELETE /api/v1/contracts/{id}
    @Operation(summary = "Delete contract")
    @Permission(name="DELETE CONTRACT", code = "DELETE_CONTRACT")
    @DeleteMapping("/contracts/{id}")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> deleteContract(@PathVariable("id") String contractId, Authentication authentication) {
        ContractResponseDto deletedContract = contractService.deleteContract(contractId, authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(null, deletedContract, "Contract deleted successfully", null);
    }

    // POST /api/v1/contracts/{id}/send
    @Operation(summary = "Send contract")
    @Permission(name="SEND CONTRACT", code = "SEND_CONTRACT")
    @PostMapping("/contracts/{id}/send")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> sendContract(@PathVariable("id") String contractId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        ContractResponseDto sentContract = contractService.sendContract(contractId, userId);
        return apiResponseUtil.getResponse(null, sentContract, "Contract sent successfully", null);
    }

   // POST /api/v1/contracts/{id}/reject
    @Operation(summary = "Reject contract")
    @Permission(name="REJECT CONTRACT", code = "REJECT_CONTRACT")
    @PostMapping("/contracts/{id}/reject")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> rejectContract(@PathVariable("id") String contractId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        ContractResponseDto rejectedContract = contractService.rejectContract(contractId, userId);
        return apiResponseUtil.getResponse(null, rejectedContract, "Contract rejected successfully", null);
    }

    // POST /api/v1/contracts/{id}/approve
    @Operation(summary = "Approve contract")
    @Permission(name="APPROVE CONTRACT", code = "APPROVE_CONTRACT")
    @PostMapping("/contracts/{id}/approve")
    public ApiResponseUtil.ApiResponseEntity<ContractResponseDto> approveContract(@PathVariable("id") String contractId, Authentication authentication) {
        UUID userId = authDetailsExtractor.getUserId(authentication);
        ContractResponseDto approvedContract = contractService.approveContract(contractId, userId);
        return apiResponseUtil.getResponse(null, approvedContract, "Contract approved successfully", null);
    }

    // GET /api/v1/contracts/{id}/download

    // GET /api/v1/contracts/{id}/subscriptions

    // GET /api/v1/contracts/{id}/jobs

    // GET /api/v1/contracts/{id}/invoices

    // GET /api/v1/contracts/{id}/payments

}
