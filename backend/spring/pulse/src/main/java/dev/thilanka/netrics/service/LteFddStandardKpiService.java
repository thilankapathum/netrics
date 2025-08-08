package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddStandardKpiDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddStandardKpiService {
    List<LteFddStandardKpiDto> getAll();

    LteFddStandardKpiDto createLteFddStandardKpi(@Valid LteFddStandardKpiDto lteFddStandardKpiDto);

    List<LteFddStandardKpiDto> createLteFddStandardKpis(@Valid List<LteFddStandardKpiDto> lteFddStandardKpiDtos);
}
