package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AreaDistrictCodeMappingDto;
import dev.thilanka.netrics.entity.AreaDistrictCodeMapping;

import java.util.List;

public interface AreaDistrictCodeMappingService {

    AreaDistrictCodeMapping createAreaDistrictCodeMapping(AreaDistrictCodeMapping mapping);

    AreaDistrictCodeMappingDto createAreaDistrictCodeMapping(AreaDistrictCodeMappingDto dto);

    List<AreaDistrictCodeMappingDto> createAreaDistrictCodeMappings(List<AreaDistrictCodeMappingDto> dtos);

    List<AreaDistrictCodeMappingDto> getAll();

    void deleteAreaDistrictCodeMapping(Long id);
}
