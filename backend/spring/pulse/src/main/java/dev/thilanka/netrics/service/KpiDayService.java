package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.KpiDay;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiDayService {

    boolean checkImproved(String worstOrder, Double difference);
    List<KpiDataDto> findAll();

    KpiDataDto createLteFddKpiDay(KpiDay kpiDay);

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period, String ratName, String granularityName);

    BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshotsWithDistrict(String basicKpiName, String period, String districtName, String ratName, String granularityName);

    List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName, String granularityName);

    List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period, String ratName, String granularityName);

    List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period, String ratName, String granularityName);

    List<KpiTrendDto> getTrendByKpiAndDistrict(String standardKpiName, String period, String districtName, String ratName, String granularityName);

    List<WorstCellsDto> getWorstCellsByKpi(String kpiName, String period, boolean excludeZeroes, String ratName, String granularityName);

//    List<WorstCellsDto> getWorstCellsByKpiExcludeZeroes(String kpiName, String period, String ratName);

    List<KpiDay> getKpiWithoutDistrict(String ratName);

    List<WorstCellsDto> getWorstCellsByKpiAndDistrict(String kpiName, String period, boolean excludeZeroes, String districtName, String ratName, String granularityName);

//    List<WorstCellsDto> getWorstCellsByKpiAndDistrictExcludeZeroes(String kpiName, String period, String districtName, String ratName);

    List<CellNameDto> getCellNamesByTimestamps(LocalDateTime timestamp, LocalDateTime preTimestamp, String ratName, String granularityName);
}
