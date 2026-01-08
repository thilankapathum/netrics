package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.WorstCellSaveDto;
import dev.thilanka.netrics.dto.WorstCellsWithLatestDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface WorstCellDashboardService {

    public WorstCellSaveDto createWorstCell(WorstCellSaveDto worstCellSaveDto, String period, String areaName, LocalDateTime timestamp);

    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, String ratName, String granularityName);

    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, LocalDateTime timestamp, String ratName, String granularityName);

    public Map<String,List<WorstCellSaveDto>> createWorstCellsByRatAndAreaType(String period, String areaType, LocalDateTime timestamp, String ratName, String granularityName);

    public List<WorstCellsWithLatestDto> getWorstCellsByKpiAndArea(String timestamp, String kpiName, String period, boolean excludeZeroes, String areaName, String ratName, String granularityName);

    public List<Timestamp> getTimestamps(String kpiName, String period, String areaName, String ratName, String granularityName);

}
