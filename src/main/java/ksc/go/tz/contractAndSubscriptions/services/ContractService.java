package ksc.go.tz.contractAndSubscriptions.services;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.UUID;

public interface ContractService {

    ContractResponseDto addContract(ContractDto contractDto, UUID createdBy);

    Page<ContractResponseDto> getAllContractsWithPaginationAndSortingAndFiltering(int page, int size, String sortBy, String sortDir, String serviceLine, UUID userId);

    ContractResponseDto updateContract(String contractId, ContractDto contractDto, UUID userId);

    ContractResponseDto deleteContract(String contractId, UUID userId);

    ContractResponseDto sendContract(String contractId, UUID userId);

    ContractResponseDto rejectContract(String contractId, UUID userId);

    ContractResponseDto approveContract(String contractId, UUID userId);

    List<ContractResponseDto> getAll(UUID userId);

}
