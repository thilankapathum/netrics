package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import jakarta.validation.Valid;

import java.util.List;

public interface StandardRawKpiMappingService {
    List<StandardRawKpiMappingDto> getAll(String ratName);

    StandardRawKpiMappingDto createMapping(StandardRawKpiMappingDto dto);

    List<StandardRawKpiMappingDto> createMappingList(@Valid List<StandardRawKpiMappingDto> dtos);

    boolean isKpiMappingAvailable(String ratName, String standardKpiName);
}
