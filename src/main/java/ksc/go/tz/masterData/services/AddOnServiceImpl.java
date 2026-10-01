package ksc.go.tz.masterData.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.Status;
import ksc.go.tz.masterData.dto.AddOnDto;
import ksc.go.tz.masterData.dto.AddOnResponseDto;
import ksc.go.tz.masterData.entities.AddOn;
import ksc.go.tz.masterData.repository.AddOnRepository;
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
public class AddOnServiceImpl implements AddOnService {

    private final AddOnRepository addOnRepository;

    @Override
    public AddOnResponseDto addAddOn(AddOnDto addOnDto, UUID createdBy) {
        if (addOnRepository.existsByNameIgnoreCase(addOnDto.getName())) {
            throw new AfriException("Add-on with name '" + addOnDto.getName() + "' already exists");
        }
        AddOn addOn = new AddOn();
        addOn.setName(addOnDto.getName());
        addOn.setPrice(addOnDto.getPrice());
        addOn.setDescription(addOnDto.getDescription());
        addOn.setStatus(Status.ACTIVE);
        addOn.setCreatedBy(createdBy);
        addOn.setCreatedAt(LocalDateTime.now());
        return new AddOnResponseDto(addOnRepository.save(addOn));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddOnResponseDto> getAll(UUID userId) {
        return addOnRepository.findAll().stream().map(AddOnResponseDto::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AddOnResponseDto> getById(String addOnId) {
        return addOnRepository.findById(parseId(addOnId)).map(AddOnResponseDto::new);
    }

    @Override
    public AddOnResponseDto updateAddOn(String addOnId, AddOnDto addOnDto, UUID userId) {
        AddOn addOn = findAddOn(addOnId);
        if (addOnRepository.existsByNameIgnoreCaseAndIdNot(addOnDto.getName(), addOn.getId())) {
            throw new AfriException("Add-on with name '" + addOnDto.getName() + "' already exists");
        }
        addOn.setName(addOnDto.getName());
        addOn.setPrice(addOnDto.getPrice());
        addOn.setDescription(addOnDto.getDescription());
        addOn.setUpdatedBy(userId);
        addOn.setUpdatedAt(LocalDateTime.now());
        return new AddOnResponseDto(addOnRepository.save(addOn));
    }

    @Override
    public AddOnResponseDto deleteById(String addOnId, UUID userId) {
        AddOn addOn = findAddOn(addOnId);
        addOn.setDeletedAt(LocalDateTime.now());
        addOn.setDeleted(true);
        addOn.setUpdatedBy(userId);
        return new AddOnResponseDto(addOnRepository.save(addOn));
    }

    @Override
    public AddOnResponseDto changeStatus(String addOnId, String status, UUID userId) {
        AddOn addOn = findAddOn(addOnId);
        try {
            addOn.setStatus(Status.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid status value: " + status);
        }
        addOn.setUpdatedBy(userId);
        addOn.setUpdatedAt(LocalDateTime.now());
        return new AddOnResponseDto(addOnRepository.save(addOn));
    }

    private AddOn findAddOn(String addOnId) {
        return addOnRepository.findById(parseId(addOnId))
                .orElseThrow(() -> new AfriException("Add-on not found"));
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid add-on ID: " + id);
        }
    }
}
