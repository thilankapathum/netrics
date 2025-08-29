package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

//    List<WorstCell> getWorstCellsByKpi(String kpiName, String period, int count);

    KpiDataDto createLteFddKpiDay(LteFddKpiDay kpiDay);

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period);

    List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period);

    List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period);

    List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period);

    Page<WorstCellsDto> getWorstCellsByKpiPage(String kpiName, String period, int page, int size);

    List<LteFddKpiDay> getKpiWithoutDistrict();

    Page<WorstCellsDto> getWorstCellsByKpiAndDistrictPage(String kpiName, String period, String districtName, int page, int size);

}
