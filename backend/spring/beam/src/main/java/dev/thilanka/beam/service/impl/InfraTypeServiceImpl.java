package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.exception.DataNotChangedException;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.InfraTypeDto;
import dev.thilanka.beam.entity.InfraType;
import dev.thilanka.beam.repository.InfraTypeRepository;
import dev.thilanka.beam.service.InfraTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfraTypeServiceImpl implements InfraTypeService {
    private final InfraTypeRepository infraTypeRepository;

    @Override
    public InfraType createInfraType(InfraType infraType) {
        return infraTypeRepository.save(infraType);
    }

    @Override
    public InfraTypeDto createInfraType(InfraTypeDto dto) {
        return toDto(createInfraType(toInfraType(dto)));
    }

    @Override
    public List<InfraTypeDto> createInfraTypes(List<InfraTypeDto> dtos) {
        List<InfraTypeDto> infraTypeDtos = new ArrayList<>();

        for (InfraTypeDto dto : dtos) {
            try {
                infraTypeDtos.add(createInfraType(dto));
            } catch (Exception e) {
                log.warn("Error creating Infra-Type by infraType={} legType={}", dto.infraType(), dto.legType(), e);
            }
        }
        return infraTypeDtos;
    }

    @Override
    public InfraType updateInfraType(InfraType infraType) {

        InfraType existingInfraType = findById(infraType.getId());

        if (existingInfraType.getInfraType().equals(infraType.getInfraType())
                && existingInfraType.getLegType().equals(infraType.getLegType())) {
            throw new DataNotChangedException("Infra-Type", "infra-type & leg-type",
                    infraType.getInfraType() + "-" + infraType.getLegType());
        }
        existingInfraType.setInfraType(infraType.getInfraType());
        existingInfraType.setLegType(infraType.getLegType());

        return infraTypeRepository.save(existingInfraType);
    }

    @Override
    public InfraTypeDto updateInfraType(InfraTypeDto dto) {
        return toDto(updateInfraType(toInfraType(dto)));
    }

    @Override
    public List<InfraTypeDto> updateInfraTypes(List<InfraTypeDto> dtos) {
        List<InfraTypeDto> infraTypeDtos = new ArrayList<>();

        for (InfraTypeDto dto : dtos) {
            try {
                infraTypeDtos.add(updateInfraType(dto));
            } catch (Exception e) {
                log.warn("Error updating Infra-Type by infraType={} legType={}", dto.infraType(), dto.legType(), e);
            }
        }
        return infraTypeDtos;
    }

    @Override
    public InfraType findById(Long id) {
        return infraTypeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Infra-Type", "id", id));
    }

    @Override
    public InfraTypeDto getById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public List<InfraType> findAllInfraTypes() {
        return infraTypeRepository.findAll();
    }

    @Override
    public List<InfraTypeDto> getAllInfraTypes() {
        List<InfraType> infraTypes = findAllInfraTypes();
        return infraTypes.stream().map(this::toDto).collect(Collectors.toList());
    }

    private InfraTypeDto toDto(InfraType infraType) {
        return new InfraTypeDto(infraType.getId(), infraType.getInfraType(), infraType.getLegType());
    }

    private InfraType toInfraType(InfraTypeDto dto) {
        return InfraType.builder()
                .legType(dto.legType())
                .infraType(dto.infraType())
                .build();
    }
}
