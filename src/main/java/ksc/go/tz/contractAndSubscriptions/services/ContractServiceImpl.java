package ksc.go.tz.contractAndSubscriptions.services;

import ksc.go.tz.billing.dto.InvoiceResponseDto;
import ksc.go.tz.billing.dto.PaymentResponseDto;
import ksc.go.tz.billing.repository.InvoiceRepository;
import ksc.go.tz.billing.repository.PaymentRepository;
import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;
import ksc.go.tz.contractAndSubscriptions.repository.SubscriptionRepository;
import afriUtils.responses.AfriException;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.billing.services.InvoiceService;
import ksc.go.tz.common.ReferenceNumberGenerator;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractReasonDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractSignDto;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.repository.ContractRepository;
import ksc.go.tz.enums.ContractStatus;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.QuoteStatus;
import ksc.go.tz.enums.SignatureStatus;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.quotation.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ContractServiceImpl implements ContractService {

    /** Fields the paginated list may be sorted by; anything else falls back to createdAt. */
    private static final Set<String> SORTABLE = Set.of("createdAt", "startDate", "endDate", "contractValue", "status", "contractNumber");

    private final ContractRepository contractRepository;
    private final QuoteRepository quoteRepository;
    private final InvoiceService invoiceService;
    private final ContractPdfRenderer contractPdfRenderer;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public ContractResponseDto addContract(ContractDto contractDto, UUID createdBy) {
        if (contractDto.getQuoteId() == null || contractDto.getQuoteId().isBlank()) {
            throw new AfriException("Quote is required to create a contract");
        }
        Quote quote = quoteRepository.findById(parseId(contractDto.getQuoteId(), "quote"))
                .orElseThrow(() -> new AfriException("Quote not found"));
        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new AfriException("Quotation " + quote.getQuoteNumber() + " is " + quote.getStatus()
                    + "; only ACCEPTED quotations can become contracts.");
        }
        if (contractRepository.existsByQuoteId(quote.getId())) {
            throw new AfriException("Quotation " + quote.getQuoteNumber() + " already has a contract.");
        }
        validateDates(contractDto);

        Contract contract = new Contract();
        contract.setContractNumber(ReferenceNumberGenerator.next("CT"));
        contract.setQuote(quote);
        contract.setSite(quote.getSite());
        contract.setServiceType(quote.getServiceType() != null ? quote.getServiceType() : LeadServiceType.OTHER);
        contract.setStatus(ContractStatus.DRAFT);
        contract.setSignatureStatus(SignatureStatus.PENDING);
        applyEditableFields(contract, contractDto, quote);
        contract.setCreatedBy(createdBy);
        contract.setCreatedAt(LocalDateTime.now());
        Contract saved = contractRepository.save(contract);

        invoiceService.attachContract(quote, saved);
        log.info("[CONTRACT] {} created from quotation {}", saved.getContractNumber(), quote.getQuoteNumber());
        return new ContractResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractResponseDto> getAllContractsWithPaginationAndSortingAndFiltering(int page, int size, String sortBy, String sortDir,
                                                                                         String status, String serviceLine, String clientId) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = SORTABLE.contains(sortBy) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(direction, sortField));

        Specification<Contract> specification = (root, query, cb) -> cb.conjunction();
        if (status != null && !status.isBlank()) {
            ContractStatus wanted = parseEnum(ContractStatus.class, status, "status");
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), wanted));
        }
        if (serviceLine != null && !serviceLine.isBlank()) {
            LeadServiceType wanted = parseEnum(LeadServiceType.class, serviceLine, "service line");
            specification = specification.and((root, query, cb) -> cb.equal(root.get("serviceType"), wanted));
        }
        if (clientId != null && !clientId.isBlank()) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("clientId"), clientId.trim()));
        }
        return contractRepository.findAll(specification, pageable).map(ContractResponseDto::new);
    }

    @Override
    public ContractResponseDto updateContract(String contractId, ContractDto contractDto, UUID userId) {
        Contract contract = findContract(contractId);
        requireStatus(contract, "edited", ContractStatus.DRAFT);
        validateDates(contractDto);
        applyEditableFields(contract, contractDto, contract.getQuote());
        touch(contract, userId);
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDto> getAll(UUID userId) {
        return contractRepository.findAll().stream().map(ContractResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponseDto getContractById(String contractId) {
        return new ContractResponseDto(findContract(contractId));
    }

    @Override
    public ContractResponseDto deleteContract(String contractId, UUID userId) {
        Contract contract = findContract(contractId);
        if (!contract.getCreatedBy().equals(userId)) {
            throw new AfriException("You are not authorized to delete this contract");
        }
        requireStatus(contract, "deleted", ContractStatus.DRAFT, ContractStatus.REJECTED);
        contract.setDeletedAt(LocalDateTime.now());
        contract.setDeleted(true);
        touch(contract, userId);
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    public ContractResponseDto sendContract(String contractId, UUID userId) {
        Contract contract = findContract(contractId);
        requireStatus(contract, "sent", ContractStatus.DRAFT);
        contract.setStatus(ContractStatus.SENT);
        contract.setSentAt(LocalDateTime.now());
        touch(contract, userId);
        log.info("[CONTRACT] {} sent for signature", contract.getContractNumber());
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    public ContractResponseDto rejectContract(String contractId, ContractReasonDto reason, UUID userId) {
        Contract contract = findContract(contractId);
        requireStatus(contract, "rejected", ContractStatus.SENT);
        contract.setStatus(ContractStatus.REJECTED);
        contract.setSignatureStatus(SignatureStatus.REJECTED);
        contract.setRejectedAt(LocalDateTime.now());
        contract.setRejectionReason(reason.getReason().trim());
        touch(contract, userId);
        log.info("[CONTRACT] {} rejected: {}", contract.getContractNumber(), contract.getRejectionReason());
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    public ContractResponseDto approveContract(String contractId, ContractSignDto signature, UUID userId) {
        Contract contract = findContract(contractId);
        requireStatus(contract, "signed", ContractStatus.SENT);
        LocalDate today = LocalDate.now();
        if (contract.getEndDate() != null && contract.getEndDate().isBefore(today)) {
            throw new AfriException("Contract " + contract.getContractNumber() + " ended on " + contract.getEndDate()
                    + "; update its dates before signing.");
        }
        contract.setSignatureStatus(SignatureStatus.SIGNED);
        contract.setSignedBy(signature.getSignedBy().trim());
        contract.setSignedAt(signature.getSignedAt() != null ? signature.getSignedAt() : LocalDateTime.now());
        // Signed on or after the start date: in force straight away; otherwise the daily job activates it.
        contract.setStatus(contract.getStartDate() != null && !contract.getStartDate().isAfter(today)
                ? ContractStatus.ACTIVE
                : ContractStatus.SIGNED);
        touch(contract, userId);
        log.info("[CONTRACT] {} signed by {}; now {}", contract.getContractNumber(), contract.getSignedBy(), contract.getStatus());
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    public ContractResponseDto terminateContract(String contractId, ContractReasonDto reason, UUID userId) {
        Contract contract = findContract(contractId);
        requireStatus(contract, "terminated", ContractStatus.SIGNED, ContractStatus.ACTIVE);
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setTerminatedAt(LocalDateTime.now());
        contract.setTerminationReason(reason.getReason().trim());
        touch(contract, userId);
        log.info("[CONTRACT] {} terminated: {}", contract.getContractNumber(), contract.getTerminationReason());
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDto> getExpiring(int days) {
        if (days < 0 || days > 365) {
            throw new AfriException("days must be between 0 and 365");
        }
        LocalDate today = LocalDate.now();
        return contractRepository.findByStatusAndEndDateBetweenOrderByEndDateAsc(ContractStatus.ACTIVE, today, today.plusDays(days))
                .stream().map(ContractResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetaData generatePdf(String contractId) {
        Contract contract = findContract(contractId);
        String number = contract.getContractNumber() != null ? contract.getContractNumber() : "CT-" + contract.getId();
        return new FileMetaData(contractPdfRenderer.render(contract), "application/pdf", number + ".pdf");
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getInvoices(String contractId) {
        Contract contract = findContract(contractId);
        return invoiceRepository.findByContractIdOrderByCreatedAtDesc(contract.getId()).stream()
                .map(InvoiceResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getSubscriptions(String contractId) {
        Contract contract = findContract(contractId);
        return subscriptionRepository.findByContractIdOrderByCreatedAtDesc(contract.getId()).stream()
                .map(subscription -> new SubscriptionResponseDto(subscription, false)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPayments(String contractId) {
        Contract contract = findContract(contractId);
        return paymentRepository.findByInvoiceContractIdOrderByCreatedAtDesc(contract.getId()).stream()
                .map(PaymentResponseDto::new).toList();
    }

    @Override
    public int applyDateTransitions() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int changed = 0;
        for (Contract contract : contractRepository.findByStatusAndStartDateLessThanEqual(ContractStatus.SIGNED, today)) {
            // A signed contract whose whole period has already passed goes straight to EXPIRED.
            boolean ended = contract.getEndDate() != null && contract.getEndDate().isBefore(today);
            contract.setStatus(ended ? ContractStatus.EXPIRED : ContractStatus.ACTIVE);
            contract.setUpdatedAt(now);
            contractRepository.save(contract);
            log.info("[CONTRACT] {} is now {} (start date {})", contract.getContractNumber(), contract.getStatus(), contract.getStartDate());
            changed++;
        }
        for (Contract contract : contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, today)) {
            contract.setStatus(ContractStatus.EXPIRED);
            contract.setUpdatedAt(now);
            contractRepository.save(contract);
            log.info("[CONTRACT] {} expired (end date {})", contract.getContractNumber(), contract.getEndDate());
            changed++;
        }
        return changed;
    }

    // ------------------------------------------------------------------ helpers

    private void applyEditableFields(Contract contract, ContractDto dto, Quote quote) {
        String clientId = dto.getClientId() != null && !dto.getClientId().isBlank()
                ? dto.getClientId().trim()
                : (quote.getSite() != null ? quote.getSite().getSite_owner() : null);
        if (clientId == null || clientId.isBlank()) {
            throw new AfriException("Client is required: the quote's site has no owner, so provide clientId.");
        }
        contract.setClientId(clientId);
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setFrequency(dto.getFrequency() != null ? dto.getFrequency()
                : (quote.getFrequency() != null ? quote.getFrequency() : Frequency.ONCE));
        contract.setContractValue(dto.getContractValue() != null ? dto.getContractValue() : quote.getPriceMax());
        contract.setBusinessInfo(blankToNull(dto.getBusinessInfo()));
        contract.setBusinessTin(blankToNull(dto.getBusinessTin()));
        contract.setBusinessBrelaNo(blankToNull(dto.getBusinessBrelaNo()));
        contract.setPersonalIdNo(blankToNull(dto.getPersonalIdNo()));
        contract.setTerms(blankToNull(dto.getTerms()));
    }

    private static void validateDates(ContractDto dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new AfriException("Start date and end date are required");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new AfriException("End date must be on or after the start date");
        }
    }

    private static void requireStatus(Contract contract, String action, ContractStatus... allowed) {
        for (ContractStatus status : allowed) {
            if (contract.getStatus() == status) {
                return;
            }
        }
        throw new AfriException("Contract " + contract.getContractNumber() + " is " + contract.getStatus()
                + " and cannot be " + action + " (allowed from: " + List.of(allowed) + ").");
    }

    private static void touch(Contract contract, UUID userId) {
        contract.setUpdatedBy(userId);
        contract.setUpdatedAt(LocalDateTime.now());
    }

    private Contract findContract(String contractId) {
        return contractRepository.findById(parseId(contractId, "contract"))
                .orElseThrow(() -> new AfriException("Contract not found"));
    }

    private static UUID parseId(String id, String label) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AfriException("Invalid " + label + " ID: " + id);
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String label) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid " + label + ": " + value);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
