package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.*;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    KpiSnapshot getLatestCalculatedKpiSnapshot(String kpiName, String period, boolean isPrevious);

    List<FinalKpiSnapshot> getLatestBasicAndStandardKpiSnapshot(String basicKpiName, String period);

//    List<WorstCellKpiData> findWorstCellsByKpi(String basicKpiName, String period, int count);

//    List<WorstCellKpiDataCurrPre> getWorstCellsByKpiWithPre(String basicKpiName, String period, int count);

    List<FinalWorstCellData> getWorstCellsByKpi(String kpiName, String period, int count);

    List<FinalKpiSnapshot> getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period);

}
