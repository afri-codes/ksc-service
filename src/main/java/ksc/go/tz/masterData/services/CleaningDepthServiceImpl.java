package ksc.go.tz.masterData.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.dto.CleaningDepthDto;
import ksc.go.tz.masterData.dto.CleaningDepthResponseDto;
import ksc.go.tz.masterData.entities.CleaningDepth;
import ksc.go.tz.masterData.repository.CleaningDepthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CleaningDepthServiceImpl implements CleaningDepthService {

    private final CleaningDepthRepository cleaningDepthRepository;

    @Override
    public CleaningDepthResponseDto addCleaningDepth(CleaningDepthDto cleaningDepthDto, UUID createdBy) {
        if (cleaningDepthRepository.existsByNameIgnoreCase(cleaningDepthDto.getName())) {
            throw new AfriException("Cleaning depth with name '" + cleaningDepthDto.getName() + "' already exists");
        }
        CleaningDepth cleaningDepth = new CleaningDepth();
        cleaningDepth.setName(cleaningDepthDto.getName());
        cleaningDepth.setPrice(cleaningDepthDto.getPrice());
        cleaningDepth.setDescription(cleaningDepthDto.getDescription());
        cleaningDepth.setStatus(Status.ACTIVE);
        cleaningDepth.setCreatedBy(createdBy);
        cleaningDepth.setCreatedAt(LocalDateTime.now());
        return new CleaningDepthResponseDto(cleaningDepthRepository.save(cleaningDepth));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CleaningDepthResponseDto> getAll(UUID userId) {
        return cleaningDepthRepository.findAll().stream().map(CleaningDepthResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CleaningDepthResponseDto> getById(String cleaningDepthId) {
        return cleaningDepthRepository.findById(parseId(cleaningDepthId)).map(CleaningDepthResponseDto::new);
    }

    @Override
    public CleaningDepthResponseDto updateCleaningDepth(String cleaningDepthId, CleaningDepthDto cleaningDepthDto, UUID userId) {
        CleaningDepth cleaningDepth = findCleaningDepth(cleaningDepthId);
        if (cleaningDepthRepository.existsByNameIgnoreCaseAndIdNot(cleaningDepthDto.getName(), cleaningDepth.getId())) {
            throw new AfriException("Cleaning depth with name '" + cleaningDepthDto.getName() + "' already exists");
        }
        cleaningDepth.setName(cleaningDepthDto.getName());
        cleaningDepth.setPrice(cleaningDepthDto.getPrice());
        cleaningDepth.setDescription(cleaningDepthDto.getDescription());
        cleaningDepth.setUpdatedBy(userId);
        cleaningDepth.setUpdatedAt(LocalDateTime.now());
        return new CleaningDepthResponseDto(cleaningDepthRepository.save(cleaningDepth));
    }

    @Override
    public CleaningDepthResponseDto deleteById(String cleaningDepthId, UUID userId) {
        CleaningDepth cleaningDepth = findCleaningDepth(cleaningDepthId);
        cleaningDepth.setDeletedAt(LocalDateTime.now());
        cleaningDepth.setDeleted(true);
        cleaningDepth.setUpdatedBy(userId);
        return new CleaningDepthResponseDto(cleaningDepthRepository.save(cleaningDepth));
    }

    @Override
    public CleaningDepthResponseDto changeStatus(String cleaningDepthId, String status, UUID userId) {
        CleaningDepth cleaningDepth = findCleaningDepth(cleaningDepthId);
        try {
            cleaningDepth.setStatus(Status.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid status value: " + status);
        }
        cleaningDepth.setUpdatedBy(userId);
        cleaningDepth.setUpdatedAt(LocalDateTime.now());
        return new CleaningDepthResponseDto(cleaningDepthRepository.save(cleaningDepth));
    }

    private CleaningDepth findCleaningDepth(String cleaningDepthId) {
        return cleaningDepthRepository.findById(parseId(cleaningDepthId))
                .orElseThrow(() -> new AfriException("Cleaning depth not found"));
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid cleaning depth ID: " + id);
        }
    }
}
