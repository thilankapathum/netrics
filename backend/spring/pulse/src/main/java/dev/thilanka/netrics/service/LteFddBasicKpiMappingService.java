package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddBasicKpiMappingDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddBasicKpiMappingService {
    List<LteFddBasicKpiMappingDto> getAll();

    LteFddBasicKpiMappingDto createMapping(@Valid LteFddBasicKpiMappingDto dto);
}
