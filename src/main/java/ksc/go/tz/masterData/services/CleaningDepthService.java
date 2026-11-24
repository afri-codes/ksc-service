package ksc.go.tz.masterData.services;

import ksc.go.tz.masterData.dto.CleaningDepthDto;
import ksc.go.tz.masterData.dto.CleaningDepthResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CleaningDepthService {

    CleaningDepthResponseDto addCleaningDepth(CleaningDepthDto cleaningDepthDto, UUID createdBy);

    List<CleaningDepthResponseDto> getAll(UUID userId);

    Optional<CleaningDepthResponseDto> getById(String cleaningDepthId);

    CleaningDepthResponseDto updateCleaningDepth(String cleaningDepthId, CleaningDepthDto cleaningDepthDto, UUID userId);

    CleaningDepthResponseDto deleteById(String cleaningDepthId, UUID userId);

    CleaningDepthResponseDto changeStatus(String cleaningDepthId, String status, UUID userId);
}
