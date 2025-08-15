package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddKpiMappingToOssDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddKpiMappingToOssService {
    List<LteFddKpiMappingToOssDto> getAll();

    LteFddKpiMappingToOssDto createLteFddKpiMapping(@Valid LteFddKpiMappingToOssDto dto);

    List<LteFddKpiMappingToOssDto> createLteFddKpiMappingList(@Valid List<LteFddKpiMappingToOssDto> dtos);
}
