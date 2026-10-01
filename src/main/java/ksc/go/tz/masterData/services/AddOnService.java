package ksc.go.tz.masterData.services;

import ksc.go.tz.masterData.dto.AddOnDto;
import ksc.go.tz.masterData.dto.AddOnResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddOnService {

    AddOnResponseDto addAddOn(AddOnDto addOnDto, UUID createdBy);

    List<AddOnResponseDto> getAll(UUID userId);

    Optional<AddOnResponseDto> getById(String addOnId);

    AddOnResponseDto updateAddOn(String addOnId, AddOnDto addOnDto, UUID userId);

    AddOnResponseDto deleteById(String addOnId, UUID userId);

    AddOnResponseDto changeStatus(String addOnId, String status, UUID userId);
}
