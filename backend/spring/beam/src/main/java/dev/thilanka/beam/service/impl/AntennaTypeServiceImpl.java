package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.exception.DataNotChangedException;
import dev.thilanka.beam.common.exception.DependencyConflictException;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.AntennaTypeDto;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.AntennaType;
import dev.thilanka.beam.repository.AntennaRepository;
import dev.thilanka.beam.repository.AntennaTypeRepository;
import dev.thilanka.beam.service.AntennaTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AntennaTypeServiceImpl implements AntennaTypeService {
    private final AntennaTypeRepository antennaTypeRepository;
    private final AntennaRepository antennaRepository;

    @Override
    public AntennaType createAntennaType(AntennaType antennaType) {
        return antennaTypeRepository.save(antennaType);
    }

    @Override
    public AntennaTypeDto createAntennaType(AntennaTypeDto dto) {
        return toDto(createAntennaType(toEntity(dto)));
    }

    @Override
    public List<AntennaTypeDto> createAntennaTypes(List<AntennaTypeDto> dtos) {
        List<AntennaTypeDto> antennaTypeDtos = new ArrayList<>();

        for (AntennaTypeDto dto : dtos) {
            try {
                antennaTypeDtos.add(createAntennaType(dto));
            } catch (Exception e) {
                log.warn("Error creating Antenna-Type name={}", dto.name(), e);
            }
        }
        return antennaTypeDtos;
    }

    @Override
    public AntennaType updateAntennaType(AntennaType antennaType) {
        AntennaType existingAntennaType = antennaTypeRepository.findById(antennaType.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Antenna-Type", "id", antennaType.getId()));

        if (existingAntennaType.getName().equals(antennaType.getName())
                && existingAntennaType.getPortCount() == antennaType.getPortCount()
                && existingAntennaType.getHeight() == antennaType.getHeight()
                && existingAntennaType.getWidth() == antennaType.getWidth()
                && existingAntennaType.isRetAvailability() == antennaType.isRetAvailability()) {
            throw new DataNotChangedException("Antenna-Type", "name", antennaType.getName());
        }

        existingAntennaType.setName(antennaType.getName());
        existingAntennaType.setPortCount(antennaType.getPortCount());
        existingAntennaType.setHeight(antennaType.getHeight());
        existingAntennaType.setWidth(antennaType.getWidth());
        existingAntennaType.setRetAvailability(antennaType.isRetAvailability());

        return antennaTypeRepository.save(existingAntennaType);
    }

    @Override
    public AntennaTypeDto updateAntennaType(AntennaTypeDto dto) {
        return toDto(updateAntennaType(toEntity(dto)));
    }

    @Override
    public List<AntennaTypeDto> updateAntennaTypes(List<AntennaTypeDto> dtos) {
        List<AntennaTypeDto> antennaTypeDtos = new ArrayList<>();

        for (AntennaTypeDto dto : dtos) {
            try {
                antennaTypeDtos.add(updateAntennaType(dto));
            } catch (Exception e) {
                log.warn("Error updating Antenna-Type name={}", dto.name(), e);
            }
        }
        return antennaTypeDtos;
    }

    @Override
    @Transactional
    public void deleteAntennaType(String name, boolean force) {
        AntennaType antennaType = findByName(name);
        List<Antenna> dependents = antennaRepository.findByAntennaType_Id(antennaType.getId());

        if (!dependents.isEmpty()) {
            if (!force) {
                throw new DependencyConflictException("Antenna-Type", name, dependents.size());
            }
            antennaRepository.nullifyAntennaType(antennaType.getId());
        }

        antennaTypeRepository.deleteByIdNative(antennaType.getId());
    }

    @Override
    public List<String> getDependentAntennaLabels(String name) {
        AntennaType antennaType = findByName(name);
        return antennaRepository.findByAntennaType_Id(antennaType.getId()).stream()
                .map(a -> a.getSector().getName() + " / Antenna " + a.getAntennaIndex())
                .toList();
    }

    @Override
    public AntennaType findByName(String name) {
        return antennaTypeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Antenna-Type", "name", name));
    }

    @Override
    public AntennaTypeDto getByName(String name) {
        return toDto(findByName(name));
    }

    @Override
    public List<AntennaType> findAll() {
        return antennaTypeRepository.findAll();
    }

    @Override
    public List<AntennaTypeDto> getAll() {
        return findAll().stream().map(this::toDto).toList();
    }

    private AntennaTypeDto toDto(AntennaType antennaType) {
        return new AntennaTypeDto(
                antennaType.getId(),
                antennaType.getName(),
                antennaType.getPortCount(),
                antennaType.getHeight(),
                antennaType.getWidth(),
                antennaType.isRetAvailability()
        );
    }

    private AntennaType toEntity(AntennaTypeDto dto) {
        return AntennaType.builder()
                .id(dto.id())
                .name(dto.name())
                .portCount(dto.portCount())
                .height(dto.height())
                .width(dto.width())
                .retAvailability(dto.retAvailability())
                .build();
    }
}
