package ksc.go.tz.masterData.services;

import ksc.go.tz.masterData.dto.SlotDto;
import ksc.go.tz.masterData.dto.SlotResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotService {

    SlotResponseDto addSlot(SlotDto slotDto, UUID createdBy);

    List<SlotResponseDto> getAll(UUID userId);

    Optional<SlotResponseDto> getById(String slotId);

    SlotResponseDto updateSlot(String slotId, SlotDto slotDto, UUID userId);

    SlotResponseDto deleteById(String slotId, UUID userId);

    SlotResponseDto changeStatus(String slotId, String status, UUID userId);
}
