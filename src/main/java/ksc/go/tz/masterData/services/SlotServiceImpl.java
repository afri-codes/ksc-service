package ksc.go.tz.masterData.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.dto.SlotDto;
import ksc.go.tz.masterData.dto.SlotResponseDto;
import ksc.go.tz.masterData.entities.Slot;
import ksc.go.tz.masterData.repository.SlotRepository;
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
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;

    @Override
    public SlotResponseDto addSlot(SlotDto slotDto, UUID createdBy) {
        validateDateRange(slotDto);
        Slot slot = new Slot();
        slot.setStartDateTime(slotDto.getStartDateTime());
        slot.setEndDateTime(slotDto.getEndDateTime());
        slot.setStatus(Status.ACTIVE);
        slot.setCreatedBy(createdBy);
        slot.setCreatedAt(LocalDateTime.now());
        return new SlotResponseDto(slotRepository.save(slot));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponseDto> getAll(UUID userId) {
        return slotRepository.findAll().stream().map(SlotResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SlotResponseDto> getById(String slotId) {
        return slotRepository.findById(parseId(slotId, "slot")).map(SlotResponseDto::new);
    }

    @Override
    public SlotResponseDto updateSlot(String slotId, SlotDto slotDto, UUID userId) {
        validateDateRange(slotDto);
        Slot slot = findSlot(slotId);
        slot.setStartDateTime(slotDto.getStartDateTime());
        slot.setEndDateTime(slotDto.getEndDateTime());
        slot.setUpdatedBy(userId);
        slot.setUpdatedAt(LocalDateTime.now());
        return new SlotResponseDto(slotRepository.save(slot));
    }

    @Override
    public SlotResponseDto deleteById(String slotId, UUID userId) {
        Slot slot = findSlot(slotId);
        slot.setDeletedAt(LocalDateTime.now());
        slot.setDeleted(true);
        slot.setUpdatedBy(userId);
        return new SlotResponseDto(slotRepository.save(slot));
    }

    @Override
    public SlotResponseDto changeStatus(String slotId, String status, UUID userId) {
        Slot slot = findSlot(slotId);
        try {
            slot.setStatus(Status.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid status value: " + status);
        }
        slot.setUpdatedBy(userId);
        slot.setUpdatedAt(LocalDateTime.now());
        return new SlotResponseDto(slotRepository.save(slot));
    }

    private Slot findSlot(String slotId) {
        return slotRepository.findById(parseId(slotId, "slot"))
                .orElseThrow(() -> new AfriException("Slot not found"));
    }

    private UUID parseId(String id, String label) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid " + label + " ID: " + id);
        }
    }

    private void validateDateRange(SlotDto slotDto) {
        if (!slotDto.getEndDateTime().isAfter(slotDto.getStartDateTime())) {
            throw new AfriException("End date time must be after start date time");
        }
    }
}
