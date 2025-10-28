package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiMappingToOssDto;
import jakarta.validation.Valid;

import java.util.List;

public interface KpiMappingToOssService {
    List<KpiMappingToOssDto> getAll(String ratName);

    KpiMappingToOssDto createKpiMappingToOss(@Valid KpiMappingToOssDto dto);

    List<KpiMappingToOssDto> createKpiMappingToOssList(@Valid List<KpiMappingToOssDto> dtos);
}
