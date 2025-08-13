package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
import dev.thilanka.netrics.entity.ltefdd.BasicStandardKpiData;
import dev.thilanka.netrics.entity.ltefdd.CompactKpiSnapshot;
import dev.thilanka.netrics.entity.ltefdd.KpiSnapshot;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    List<KpiDataFractionDto> findAllWithFractions();

    List<KpiSnapshot> getAverage();

    KpiSnapshot[] getLatestKpiSnapshot(String standardKpiName, String period);

    BasicStandardKpiData getBasicStandardKpiSnapshot(String basicKpiName, String period);

    List<CompactKpiSnapshot> getCompactBasicStandardKpiSnapshot(String basicKpiName, String period);
}
