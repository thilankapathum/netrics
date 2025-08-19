package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddStandardKpiService {
    List<StandardKpiDto> getAll();

    StandardKpiDto createKpi(@Valid StandardKpiDto dto);

    List<StandardKpiDto> createKpis(@Valid List<StandardKpiDto> dtos);

    LteFddStandardKpi findByKpiName(String s);
}
