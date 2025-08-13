package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDaySnap;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    List<KpiDataFractionDto> findAllWithFractions();

    List<LteFddKpiDaySnap> getAverage();

    LteFddKpiDaySnap[] getKpiSnapshot(String standardKpiName, String period);
}
