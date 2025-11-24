package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DashboardWorstCellDto;
import dev.thilanka.netrics.dto.WorstCellsDto;

import java.util.List;

public interface WorstCellDashboardService {

    public DashboardWorstCellDto createWorstCell(DashboardWorstCellDto dashboardWorstCell, String period, String areaAggregation);

    public List<DashboardWorstCellDto> createWorstCellsByKpi(String kpiName, String period, String areaAggregation, boolean excludeZeroes, String ratName);

    public List<DashboardWorstCellDto> createWorstCellsByKpiAndDistrict(String kpiName, String period, boolean excludeZeroes, String districtName, String ratName);

    public List<DashboardWorstCellDto> getWorstCellsByKpiAndArea(String timestamp,String kpiName, String period, boolean excludeZeroes, String areaAggregation, String ratName);

}
