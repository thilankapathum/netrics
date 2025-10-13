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

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period, String ratName);

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshotsWithDistrict(String basicKpiName, String period, String districtName, String ratName);

    List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName);

    List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period, String ratName);

    List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period, String ratName);

    List<KpiTrendDto> getTrendByKpiAndDistrict(String standardKpiName, String period, String districtName, String ratName);

    List<WorstCellsDto> getWorstCellsByKpi(String kpiName, String period, String ratName);

    List<WorstCellsDto> getWorstCellsByKpiExcludeZeroes(String kpiName, String period, String ratName);

    List<LteFddKpiDay> getKpiWithoutDistrict();

    List<WorstCellsDto> getWorstCellsByKpiAndDistrict(String kpiName, String period, String districtName, String ratName);

    List<WorstCellsDto> getWorstCellsByKpiAndDistrictExcludeZeroes(String kpiName, String period, String districtName, String ratName);

}
