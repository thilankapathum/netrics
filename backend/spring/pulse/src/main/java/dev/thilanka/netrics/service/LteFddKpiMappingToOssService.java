package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiMappingToOssDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddKpiMappingToOssService {
    List<KpiMappingToOssDto> getAll();

    KpiMappingToOssDto createLteFddKpiMapping(@Valid KpiMappingToOssDto dto);

    List<KpiMappingToOssDto> createLteFddKpiMappingList(@Valid List<KpiMappingToOssDto> dtos);
}
