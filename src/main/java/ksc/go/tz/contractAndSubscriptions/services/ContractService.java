package ksc.go.tz.contractAndSubscriptions.services;

import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractReasonDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractSignDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ContractService {

    /** Creates a DRAFT contract from an ACCEPTED quote and links the quote's invoice to it. */
    ContractResponseDto addContract(ContractDto contractDto, UUID createdBy);

    Page<ContractResponseDto> getAllContractsWithPaginationAndSortingAndFiltering(int page, int size, String sortBy, String sortDir,
                                                                                  String status, String serviceLine, String clientId);

    /** Only DRAFT contracts can be edited. */
    ContractResponseDto updateContract(String contractId, ContractDto contractDto, UUID userId);

    /** Only DRAFT or REJECTED contracts can be deleted. */
    ContractResponseDto deleteContract(String contractId, UUID userId);

    /** DRAFT → SENT. */
    ContractResponseDto sendContract(String contractId, UUID userId);

    /** SENT → REJECTED. */
    ContractResponseDto rejectContract(String contractId, ContractReasonDto reason, UUID userId);

    /** SENT → SIGNED (or straight to ACTIVE when the start date has arrived). */
    ContractResponseDto approveContract(String contractId, ContractSignDto signature, UUID userId);

    /** SIGNED / ACTIVE → TERMINATED. */
    ContractResponseDto terminateContract(String contractId, ContractReasonDto reason, UUID userId);

    List<ContractResponseDto> getAll(UUID userId);

    ContractResponseDto getContractById(String contractId);

    /** ACTIVE contracts whose end date falls within the next {@code days} days, soonest first. */
    List<ContractResponseDto> getExpiring(int days);

    FileMetaData generatePdf(String contractId);

    /** Invoices linked to the contract, newest first. */
    List<InvoiceResponseDto> getInvoices(String contractId);

    /** Subscriptions under the contract, newest first. */
    List<SubscriptionResponseDto> getSubscriptions(String contractId);

    /** Payments for any of the contract's invoices, newest first. */
    List<PaymentResponseDto> getPayments(String contractId);

    /** SIGNED → ACTIVE once started, ACTIVE → EXPIRED once ended. Returns how many contracts changed. */
    int applyDateTransitions();
}
