package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AreaTypeDto;
import dev.thilanka.netrics.entity.AreaType;

import java.util.List;

public interface AreaTypeService {

    AreaType createAreaType(AreaType areaType);

    AreaTypeDto createAreaType(AreaTypeDto dto);

    List<AreaTypeDto> createAreaTypes(List<AreaTypeDto> dtos);

    AreaType findAreaTypeByName(String name);

    List<AreaTypeDto> getAll();
}
