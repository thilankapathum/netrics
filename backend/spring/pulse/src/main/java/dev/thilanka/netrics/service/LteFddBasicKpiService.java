package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddBasicKpiService {
    List<BasicKpiDto> getAll();

    BasicKpiDto createBasicKpi(@Valid BasicKpiDto dto);

    LteFddBasicKpi findByKpiName(String kpiName);
}
