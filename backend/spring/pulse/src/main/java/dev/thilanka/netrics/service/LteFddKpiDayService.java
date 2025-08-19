package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.*;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    List<WorstCell> getWorstCellsByKpi(String kpiName, String period, int count);

    List<KpiSnapshot> getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period);

    List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period);

}
