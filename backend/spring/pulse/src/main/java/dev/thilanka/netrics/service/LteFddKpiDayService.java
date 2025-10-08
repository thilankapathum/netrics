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

    KpiDataDto createLteFddKpiDay(LteFddKpiDay kpiDay);

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period);

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshotsWithDistrict(String basicKpiName, String period, String districtName);

    List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period);

    List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period);

    List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period);

    List<KpiTrendDto> getTrendByKpiAndDistrict(String standardKpiName, String period, String districtName);

    List<WorstCellsDto> getWorstCellsByKpi(String kpiName, String period);

    List<WorstCellsDto> getWorstCellsByKpiExcludeZeroes(String kpiName, String period);

    List<LteFddKpiDay> getKpiWithoutDistrict();

    List<WorstCellsDto> getWorstCellsByKpiAndDistrict(String kpiName, String period, String districtName);

    List<WorstCellsDto> getWorstCellsByKpiAndDistrictExcludeZeroes(String kpiName, String period, String districtName);

}
