package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddStandardRawKpiMappingDto;

import java.util.List;

public interface LteFddStandardRawKpiMappingService {
    List<LteFddStandardRawKpiMappingDto> getAll();

    LteFddStandardRawKpiMappingDto createMapping(LteFddStandardRawKpiMappingDto dto);
}
