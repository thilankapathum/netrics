package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.AreaTypeDto;
import dev.thilanka.netrics.entity.Area;

import java.util.List;

public interface AreaService {

    AreaDto createArea(AreaDto areaDto);

    List<AreaDto> createAreas(List<AreaDto> dtos);

    AreaDto getAllAreas();

    Area findAreaByName(String name);

    AreaDto getAreaByName(String name);

    List<AreaDto> getAreasByAreaType(String areaTypeName);
}
