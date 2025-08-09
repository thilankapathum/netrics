package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    List<KpiDataFractionDto> findAllWithFractions();
}
