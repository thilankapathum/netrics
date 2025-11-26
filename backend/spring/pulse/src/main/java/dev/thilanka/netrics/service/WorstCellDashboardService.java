package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DashboardWorstCellDto;
import dev.thilanka.netrics.dto.WorstCellsDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface WorstCellDashboardService {

    public DashboardWorstCellDto createWorstCell(DashboardWorstCellDto dashboardWorstCell, String period, String areaName);

//    public List<DashboardWorstCellDto> createWorstCellsByKpi(String kpiName, String period, String areaAggregation, boolean excludeZeroes, String ratName);

    public List<DashboardWorstCellDto> createWorstCellsByKpiAndDistrict(String kpiName, String period, boolean excludeZeroes, String districtName, String ratName);

    public List<DashboardWorstCellDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, String ratName);

    public List<DashboardWorstCellDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, LocalDateTime timestamp, String ratName);

    public List<DashboardWorstCellDto> getWorstCellsByKpiAndArea(String timestamp,String kpiName, String period, boolean excludeZeroes, String areaName, String ratName);
//    public List<DashboardWorstCellDto> getWorstCellsByKpiAndArea(LocalDateTime timestamp,String kpiName, String period, boolean excludeZeroes, String areaName, String ratName);

    public List<Timestamp> getTimestamps(String kpiName, String period, String areaName, String ratName);

}
