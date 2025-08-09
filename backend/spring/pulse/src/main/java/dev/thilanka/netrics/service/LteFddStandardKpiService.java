package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddStandardKpiDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddStandardKpiService {
    List<LteFddStandardKpiDto> getAll();

    LteFddStandardKpiDto createKpi(@Valid LteFddStandardKpiDto dto);

    List<LteFddStandardKpiDto> createKpis(@Valid List<LteFddStandardKpiDto> dtos);

    LteFddStandardKpi findByKpiName(String s);
}
