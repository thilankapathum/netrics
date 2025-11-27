package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.WorstCellSaveDto;
import dev.thilanka.netrics.dto.WorstCellsDashboardDto;
import dev.thilanka.netrics.dto.WorstCellsWithLatestDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface WorstCellDashboardService {

    public WorstCellSaveDto createWorstCell(WorstCellSaveDto worstCellSaveDto, String period, String areaName);

    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, String ratName);

    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, LocalDateTime timestamp, String ratName);

    public List<WorstCellsWithLatestDto> getWorstCellsByKpiAndArea(String timestamp, String kpiName, String period, boolean excludeZeroes, String areaName, String ratName);

    public List<Timestamp> getTimestamps(String kpiName, String period, String areaName, String ratName);

}
