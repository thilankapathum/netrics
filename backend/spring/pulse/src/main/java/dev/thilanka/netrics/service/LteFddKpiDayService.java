package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.KpiSnapshot;
import dev.thilanka.netrics.entity.FinalKpiSnapshot;
import dev.thilanka.netrics.entity.WorstCellKpiData;
import dev.thilanka.netrics.entity.WorstCellKpiDataCurrPre;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    KpiSnapshot getLatestCalculatedKpiSnapshot(String kpiName, String period, boolean isPrevious);

    List<FinalKpiSnapshot> getLatestBasicAndStandardKpiSnapshot(String basicKpiName, String period);

    List<WorstCellKpiData> findWorstCellsByKpi(String basicKpiName, String period, int count);

    List<WorstCellKpiDataCurrPre> getWorstCellsByKpiWithPre(String basicKpiName, String period, int count);

    List<FinalKpiSnapshot> getFinalWorstCellsByKpi(String kpiName, String period, int count, boolean isBasic);
}
