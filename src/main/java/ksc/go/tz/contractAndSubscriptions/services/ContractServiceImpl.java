package ksc.go.tz.contractAndSubscriptions.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.repository.ContractRepository;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.SignatureStatus;
import ksc.go.tz.leads.dto.LeadsResponseDto;
import ksc.go.tz.quoting.entities.Quote;
import ksc.go.tz.quoting.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final QuoteRepository quoteRepository;


    @Override
    public ContractResponseDto addContract(ContractDto contractDto, UUID createdBy) {
        Optional<Quote> quoteOptional =  quoteRepository.findById(UUID.fromString(contractDto.getQuoteId()));
        if (quoteOptional.isEmpty()) {
            throw new AfriException("Quote is required to create a contract");
        }
        Contract contract = new Contract();
        contract.setContractValue(contractDto.getContractValue());
        contract.setServiceType(LeadServiceType.valueOf(contractDto.getServiceLine()));
        contract.setFrequency(Frequency.valueOf(contractDto.getFrequency()));
        contract.setEndDate(contractDto.getEndDate());
        contract.setStartDate(contractDto.getStartDate());
        contract.setQuote(quoteOptional.get());
        contract.setBusinessInfo(contractDto.getBusinessInfo());
        contract.setSignatureStatus(SignatureStatus.valueOf(contractDto.getSignatureStatus()));
        contract.setPersonalIdNo(contractDto.getPersonalIdNo());
        contract.setCreatedBy(createdBy);
        return null;
    }

    @Override
    public Page<ContractResponseDto> getAllContractsWithPaginationAndSortingAndFiltering(int page, int size, String sortBy, String sortDir, String serviceLine, UUID userId) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Contract> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();



        if (serviceLine != null && !serviceLine.isBlank()) {specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("serviceLine"), serviceLine));
        }

        if (userId != null) {specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId));
        }

        return contractRepository.findAll(specification, pageable).map(ContractResponseDto::new);
    }

    @Override
    public ContractResponseDto updateContract(String contractId, ContractDto contractDto, UUID userId) {
        return null;
    }


    @Override
    public List<ContractResponseDto> getAll(UUID userId) {
        return contractRepository.findAll().stream().map(ContractResponseDto::new).toList();

    }

    @Override
    public ContractResponseDto deleteContract(String contractId, UUID userId) {
        Optional<Contract> contractOptional = contractRepository.findById(UUID.fromString(contractId));
        if (contractOptional.isEmpty()) {
            throw new AfriException("Contract not found");
        }
        Contract contract = contractOptional.get();
        if (!contract.getCreatedBy().equals(userId)) {
            throw new AfriException("You are not authorized to delete this contract");
        }
        contract.setDeletedAt(java.time.LocalDateTime.now());
        return new ContractResponseDto(contractRepository.save(contract));
    }

    @Override
    public ContractResponseDto sendContract(String contractId, UUID userId) {
        return null;
    }

    @Override
    public ContractResponseDto rejectContract(String contractId, UUID userId) {
        return null;
    }

    @Override
    public ContractResponseDto approveContract(String contractId, UUID userId) {
        return null;
    }
}
