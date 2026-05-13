package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.InfraTypeDto;
import dev.thilanka.beam.entity.InfraType;

import java.util.List;

public interface InfraTypeService {

    InfraType createInfraType(InfraType  infraType);

    InfraTypeDto createInfraType(InfraTypeDto dto);

    List<InfraTypeDto> createInfraTypes(List<InfraTypeDto> dtos);

    InfraType updateInfraType(InfraType infraType);

    InfraTypeDto updateInfraType(InfraTypeDto dto);

    List<InfraTypeDto> updateInfraTypes(List<InfraTypeDto> dtos);

    InfraType findById(Long id);

    InfraTypeDto getById(Long id);

    List<InfraType> findAllInfraTypes();

    List<InfraTypeDto> getAllInfraTypes();


}
