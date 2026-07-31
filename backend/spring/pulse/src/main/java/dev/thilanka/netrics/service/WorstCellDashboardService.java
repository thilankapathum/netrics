package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.WorstCellCreationStatusDto;
import dev.thilanka.netrics.dto.WorstCellSaveDto;
import dev.thilanka.netrics.dto.WorstCellsWithLatestDto;
import dev.thilanka.netrics.entity.AreaType;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface WorstCellDashboardService {

    WorstCellSaveDto createWorstCell(WorstCellSaveDto worstCellSaveDto, String period, String areaName, LocalDateTime timestamp);

    List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, String ratName, String granularityName);

    List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, LocalDateTime timestamp, String ratName, String granularityName);

    Map<String, List<WorstCellSaveDto>> createWorstCellsByRatAndAreaType(String period, String areaType, LocalDateTime timestamp, String ratName, String granularityName);

    Map<String, List<WorstCellSaveDto>> createWeeklyWorstCellsByRatAndAreaType(String period, AreaType areaType, Rat rat, Granularity granularity);

    List<WorstCellsWithLatestDto> getWorstCellsByKpiAndArea(String timestamp, String kpiName, String period, boolean excludeZeroes, String areaName, String ratName, String granularityName);

    List<Timestamp> getTimestamps(String kpiName, String period, String areaName, String ratName, String granularityName);

    WorstCellCreationStatusDto getWorstCellCreationStatus();

    boolean isCreatingWorstCells();
}
