package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddStandardRawKpiMappingService {
    List<StandardRawKpiMappingDto> getAll();

    StandardRawKpiMappingDto createMapping(StandardRawKpiMappingDto dto);

    List<StandardRawKpiMappingDto> createMappingList(@Valid List<StandardRawKpiMappingDto> dtos);
}
