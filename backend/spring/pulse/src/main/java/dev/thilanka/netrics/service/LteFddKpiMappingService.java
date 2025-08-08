package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddKpiMappingDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddKpiMappingService {
    List<LteFddKpiMappingDto> getAll();

    LteFddKpiMappingDto createLteFddKpiMapping(@Valid LteFddKpiMappingDto dto);

    List<LteFddKpiMappingDto> createLteFddKpiMappingList(@Valid List<LteFddKpiMappingDto> dtos);
}
