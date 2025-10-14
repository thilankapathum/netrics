package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.StandardKpi;
import jakarta.validation.Valid;

import java.util.List;

public interface StandardKpiService {
    List<StandardKpiDto> getAll();

    StandardKpiDto createKpi(@Valid StandardKpiDto dto);

    List<StandardKpiDto> createKpis(@Valid List<StandardKpiDto> dtos);

    StandardKpi findByKpiName(String kpiName, String ratName);

    StandardKpi findByKpiName(String kpiName, Rat rat);

    StandardKpi findByKpiNameAndRatId(String kpiName, Long ratId);

    StandardKpi findByKpiLabel(String kpiLabel, String ratName);

    List<StandardKpiDto> getAllStandardKpiByRat(String ratName);
}
