package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.WorstCellCreationStatusDto;
import dev.thilanka.netrics.dto.WorstCellSaveDto;
import dev.thilanka.netrics.dto.WorstCellsWithLatestDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.repository.WorstCellRepository;
import dev.thilanka.netrics.service.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorstCellDashboardServiceImpl implements WorstCellDashboardService {
    private final DateService dateService;
    private final StandardKpiService standardKpiService;
    private final RatService ratService;
    private final KpiDayRepository kpiDayRepository;
    private final WorstCellRepository worstCellRepository;
    private final Mapper mapper;
    private final AreaService areaService;
    private final AreaTypeService areaTypeService;
    private final GranularityService granularityService;

    @Value("${app.worst-cell-dashboard.refresh-day}")
    private String refreshDay;

    private int totalItems = 0;
    private int executedItems = 0;

    private boolean creatingWorstCells = false;

    @Override
    public WorstCellSaveDto createWorstCell(WorstCellSaveDto worstCellSaveDto, String period, String areaName, LocalDateTime timestamp) {
        WorstCell worstCell = mapper.worstCellSaveDtoToWorstCell(worstCellSaveDto, period, areaName, timestamp);
        //worstCell.setTimestamp(timestamp);  //-- Setting the query date as the date for the worstCell (Otherwise will save another date if latest date has no KPI due to cell-outage)
        try {
            WorstCell savedWorstCell = worstCellRepository.save(worstCell);
            return mapper.toWorstCellSaveDto(savedWorstCell);
        } catch (DataIntegrityViolationException e) {
            System.out.println("Duplicate Entry. Querying existing entry...");
            WorstCell existingWorstCell = worstCellRepository.findWorstCellByCellName(
                    worstCell.getCellName(),
                    worstCell.getPeriod(),
                    worstCell.getTimestamp(),
                    worstCell.getRat().getId(),
                    worstCell.getStandardKpi().getId(),
                    worstCell.getArea().getId(),
                    worstCell.isExcludeZeroes(),
                    worstCell.getGranularity().getId()
            ).orElseThrow(() -> new RuntimeException("Worst cell query error!"));

            return mapper.toWorstCellSaveDto(existingWorstCell);
        }
    }

    @Override
    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, String ratName, String granularityName) {
        LocalDateTime timestamp = dateService.getLatestDate(ratName, granularityName);

        if (dateService.isDateIsDay(timestamp, dateService.extractDayOfWeek(refreshDay))) {
            return createWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, areaName, timestamp, ratName, granularityName);
        } else return null;
    }

    @Override
    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, LocalDateTime timestamp, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime latestTimestamp = timestamp.plusSeconds(granularity.getPlusSeconds());    //-- To get the latest time considering busy-hour KPIs (23:59:50)
        LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

        LocalDateTime preTimestamp = dateService.getPreviousDate(latestTimestamp, period);
        LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period)).toLocalDate().atStartOfDay();

        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        Area area = areaService.findAreaByName(areaName);

        List<WorstCellSaveDto> dashboardWorstCells = kpiDayRepository
                .findWorstCellsForDashboardByArea(
                        standardKpi.getId(),
                        latestTimestamp,
                        currentStart,
                        preTimestamp,
                        previousStart,
                        area.getId(),
                        rat.getId(),
                        excludeZeroes,
                        granularity.getId()
                );
        return dashboardWorstCells.stream().map(wc -> createWorstCell(wc, period, area.getName(), timestamp)).toList();
    }

    @Override
    public Map<String, List<WorstCellSaveDto>> createWorstCellsByRatAndAreaType(String period, String areaType, LocalDateTime timestamp, String ratName, String granularityName) {
        this.creatingWorstCells = true;

        boolean[] excludeZero = {false, true};
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime latestTime = timestamp.toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
        LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period)).toLocalDate().atStartOfDay();

        LocalDateTime preTimestamp = dateService.getPreviousDate(timestamp, period)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());
        LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period)).toLocalDate().atStartOfDay();

        List<Area> areas = areaService.findAreasByAreaType(areaType);

        List<StandardKpi> standardKpis = standardKpiService.findAllStandardKpiByRat(rat);

        Map<String, List<WorstCellSaveDto>> savedWorstCellsMap = new HashMap<>();

        this.totalItems = excludeZero.length * areas.size() * standardKpis.size();
        this.executedItems = 0;

        for (boolean eZ : excludeZero) {
            for (Area area : areas) {
                for (StandardKpi kpi : standardKpis) {
                    List<WorstCellSaveDto> dashboardWorstCells = kpiDayRepository.findWorstCellsForDashboardByArea(
                            kpi.getId(),
                            latestTime,     //-- Assign latestTime (including plusSeconds) to query the worstCell.
                            currentStart,
                            preTimestamp,
                            previousStart,
                            area.getId(),
                            rat.getId(),
                            eZ,
                            granularity.getId()
                    );
                    List<WorstCellSaveDto> savedWorstCells = dashboardWorstCells.stream().map(wc -> createWorstCell(wc, period, area.getName(), timestamp)).toList();  //-- Saving worst-cells to database (worst_cells table) [timestamp is used as the argument to save the worstCell with the querying timestamp (not busy-hour timestamp)]
                    savedWorstCellsMap.put(area.getName() + " - " + kpi.getKpiName(), savedWorstCells);

                    this.executedItems++;
                }
                System.out.println("[" + rat.getLabel() + " - " + granularity.getLabel() + "] Exclude Zeroes: " + eZ + " | Area: (" + areaType + ") " + area.getName());
            }
        }
        this.creatingWorstCells = false;
        this.totalItems = 0;
        this.executedItems = 0;
        return savedWorstCellsMap;
    }

    @Override
    @Cacheable(value = "dashboardWorstCells", key = "#timestamp + '_' + #kpiName + '_' + #period + '_' + #excludeZeroes + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public List<WorstCellsWithLatestDto> getWorstCellsByKpiAndArea(String timestamp, String kpiName, String period, boolean excludeZeroes, String areaName, String ratName, String granularityName) {
        LocalDateTime timestamps = dateService.extractDate(timestamp);
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime latestDate = dateService.getLatestDate(rat, granularity);
        LocalDateTime latestDateStart = latestDate.toLocalDate().atStartOfDay();

        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, rat);
        Area area = areaService.findAreaByName(areaName);

        return worstCellRepository.findWorstCellsByKpi(period, timestamps, latestDate, latestDateStart, rat.getId(), standardKpi.getId(), area.getId(), excludeZeroes, granularity.getId());
    }

    @Override
    public List<Timestamp> getTimestamps(String kpiName, String period, String areaName, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, rat);
        Area area = areaService.findAreaByName(areaName);

        return worstCellRepository.findTimestamps(period, rat.getId(), standardKpi.getId(), area.getId(), granularity.getId());
    }

    @Override
    public WorstCellCreationStatusDto getWorstCellCreationStatus() {
        return new WorstCellCreationStatusDto(this.creatingWorstCells, this.totalItems, this.executedItems);
    }

    @Override
    public boolean isCreatingWorstCells() {
        return this.creatingWorstCells;
    }

}
