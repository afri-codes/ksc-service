package ksc.go.tz.leads.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.LeadStatus;
import ksc.go.tz.leads.dto.LeadDto;
import ksc.go.tz.leads.dto.LeadsResponseDto;
import ksc.go.tz.leads.entities.Leads;
import ksc.go.tz.leads.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;

    @Override
    public LeadsResponseDto addLead(LeadDto leadDto, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Leads leads = new Leads();
        leads.setFullName(leadDto.getFullName());
        leads.setPhoneNumber(leadDto.getPhoneNumber());
        leads.setEmail(leadDto.getEmail());
        leads.setServiceTypeInterest(LeadServiceType.valueOf(leadDto.getServiceLine()));
        leads.setMessage(leadDto.getMessage());
        leads.setSource(leadDto.getSource());
        leads.setStatus(leadDto.getStatus());
        leads.setCreatedBy(createdBy);
        leads.setCreatedAt(now);
        return new LeadsResponseDto(leadRepository.save(leads));
    }

    @Override
    public LeadsResponseDto changeLeadStatus(String leadId, String status, UUID userId) {
        UUID leadUUID;

        try {
            leadUUID = UUID.fromString(leadId);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid lead ID: " + leadId);
        }

        Leads existingLead = leadRepository.findById(leadUUID)
                .orElseThrow(() -> new AfriException("Lead not found"));

        existingLead.setStatus(LeadStatus.valueOf(status));
        existingLead.setUpdatedBy(userId);
        existingLead.setUpdatedAt(LocalDateTime.now());

        Leads updatedLead = leadRepository.save(existingLead);

        return new LeadsResponseDto(updatedLead);
    }

    @Override
    public Page<LeadsResponseDto> getAllLeadsWithPaginationAndSortingAndFiltering(int page, int size, String sortBy, String sortDir, String status, String source, String serviceLine, UUID userId) {

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Leads> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (status != null && !status.isBlank()) {specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
        }

        if (source != null && !source.isBlank()) {specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("source"), source));
        }

        if (serviceLine != null && !serviceLine.isBlank()) {specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("serviceLine"), serviceLine));
        }

        if (userId != null) {specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId));
        }

        return leadRepository.findAll(specification, pageable).map(LeadsResponseDto::new);
    }

    @Override
    public List<LeadsResponseDto> getAll(UUID userId) {
        return leadRepository.findAll().stream().map(LeadsResponseDto::new).toList();

    }

    @Override
    public Optional<LeadsResponseDto> getById(String leadId) {
        Optional<Leads> site = leadRepository.findById(UUID.fromString(leadId));
        if (site.isEmpty()) {
            throw new AfriException("Site not found");
        }
        return leadRepository.findById(UUID.fromString(leadId)).map(LeadsResponseDto::new);

    }

    @Override
    public LeadsResponseDto updateLead(String site, LeadDto leadDto, UUID userId) {

        UUID siteId;

        try {
            siteId = UUID.fromString(site);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid site ID: " + site);
        }

        Leads existingSite = leadRepository.findById(siteId)
                .orElseThrow(() -> new AfriException("Site not found"));
        existingSite.setFullName(leadDto.getFullName());
        existingSite.setPhoneNumber(leadDto.getPhoneNumber());
        existingSite.setEmail(leadDto.getEmail());
        existingSite.setServiceTypeInterest(LeadServiceType.valueOf(leadDto.getServiceLine()));
        existingSite.setMessage(leadDto.getMessage());
        existingSite.setSource(leadDto.getSource());
        existingSite.setStatus(leadDto.getStatus());
        existingSite.setUpdatedBy(userId);
        existingSite.setUpdatedAt(LocalDateTime.now());

        Leads updatedSite = leadRepository.save(existingSite);

        return new LeadsResponseDto(updatedSite);
    }

    @Override
    public LeadsResponseDto deleteById(String leadId, UUID userId) {
        Optional<Leads> sites = leadRepository.findById(UUID.fromString(leadId));
        if(sites.isPresent()){
            Leads sites1 = sites.get();
            sites1.setDeletedAt(LocalDateTime.now());
            return new LeadsResponseDto(leadRepository.save(sites1));
        } else {
            throw new AfriException("Site not found");
        }
    }



}
