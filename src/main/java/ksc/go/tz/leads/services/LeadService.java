package ksc.go.tz.leads.services;



import ksc.go.tz.leads.dto.LeadDto;
import ksc.go.tz.leads.dto.LeadsResponseDto;
import org.springframework.data.domain.Page;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadService {

    List<LeadsResponseDto> getAll(UUID userId);

    LeadsResponseDto deleteById(String leadId, UUID userId);

    Optional<LeadsResponseDto> getById(String leadId);

    LeadsResponseDto updateLead(String leadId, LeadDto leadDto, UUID userId);

    LeadsResponseDto addLead(LeadDto leadDto, UUID createdBy);

    LeadsResponseDto changeLeadStatus(String leadId, String status, UUID userId);

    Page<LeadsResponseDto> getAllLeadsWithPaginationAndSortingAndFiltering(int page, int size, String sortBy, String sortDir, String status, String source, String serviceLine, UUID userId);

}
