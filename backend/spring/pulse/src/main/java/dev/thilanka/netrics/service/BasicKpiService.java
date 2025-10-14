package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.BasicKpi;
import jakarta.validation.Valid;

import java.util.List;

public interface BasicKpiService {
    List<BasicKpiDto> getAllByRat(String ratName);

    BasicKpiDto createBasicKpi(@Valid BasicKpiDto dto);

    BasicKpi findByKpiName(String kpiName, String ratName);
    BasicKpi findByKpiName(String kpiName, Rat rat);
}
