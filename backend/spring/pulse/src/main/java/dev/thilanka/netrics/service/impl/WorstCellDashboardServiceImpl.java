package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.WorstCellSaveDto;
import dev.thilanka.netrics.dto.WorstCellsDashboardDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.entity.WorstCell;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.repository.WorstCellRepository;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorstCellDashboardServiceImpl implements WorstCellDashboardService {
    private final DateService dateService;
    private final StandardKpiService standardKpiService;
    private final RatService ratService;
    private final KpiDayRepository kpiDayRepository;
    private final DistrictService districtService;
    private final WorstCellRepository worstCellRepository;
    private final Mapper mapper;
    private final AreaService areaService;

    @Value("${app.worst-cell-dashboard.refresh-day}")
    private String refreshDay;

    @Override
    public WorstCellSaveDto createWorstCell(WorstCellSaveDto dashboardWorstCell, String period, String areaName) {
        WorstCell worstCell = mapper.dashboardWorstCellDtoToWorstCell(dashboardWorstCell, period, areaName);     //TODO: Need a better saving method for Area-Aggregation
        try {
            WorstCell savedWorstCell = worstCellRepository.save(worstCell);
            return mapper.toDashboardWorstCellDto(savedWorstCell);
        } catch (DataIntegrityViolationException e) {
            System.out.println("Duplicate Entry");
            WorstCell existingWorstCell = worstCellRepository.findWorstCellByCellName(
                    worstCell.getCellName(),
                    worstCell.getPeriod(),
                    worstCell.getTimestamp(),
                    worstCell.getRat().getId(),
                    worstCell.getStandardKpi().getId(),
                    worstCell.getArea().getId()
            ).orElseThrow(() -> new RuntimeException("Worst cell query error!"));

            return mapper.toDashboardWorstCellDto(existingWorstCell);
        }
    }

    @Override
    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, String ratName) {
        LocalDateTime timestamp = dateService.getLatestDate(ratName);

        if (dateService.isDateIsDay(timestamp, dateService.extractDayOfWeek(refreshDay))) {
            return createWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, areaName, timestamp, ratName);
        } else return null;
    }

    @Override
    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, String areaName, LocalDateTime timestamp, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

        LocalDateTime preTimestamp = dateService.getPreviousDate(timestamp, period);
        LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period));

        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        Area area = areaService.findAreaByName(areaName);

        List<WorstCellSaveDto> dashboardWorstCells = kpiDayRepository
                .findWorstCellsForDashboardByArea(
                        standardKpi.getId(),
                        timestamp,
                        currentStart,
                        preTimestamp,
                        previousStart,
                        area.getId(),
                        rat.getId(),
                        excludeZeroes
                );
        return dashboardWorstCells.stream().map(wc -> createWorstCell(wc, period, area.getName())).toList();
    }

    @Override
    public List<WorstCellsDashboardDto> getWorstCellsByKpiAndArea(String timestamp, String kpiName, String period, boolean excludeZeroes, String areaName, String ratName) {
        LocalDateTime timestamps = dateService.extractDate(timestamp);
        Rat rat = ratService.findRatByName(ratName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, rat);
        Area area = areaService.findAreaByName(areaName);

        return worstCellRepository.findWorstCellsByKpi(period, timestamps, rat.getId(), standardKpi.getId(), area.getId());
//        return worstCells.stream().map(ws -> mapper.toDashboardWorstCellDto(ws)).toList();
    }

    @Override
    public List<Timestamp> getTimestamps(String kpiName, String period, String areaName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName,rat);
        Area area = areaService.findAreaByName(areaName);

        return worstCellRepository.findTimestamps(period,rat.getId(),standardKpi.getId(),area.getId());
    }

    @Override
    public List<WorstCellSaveDto> createWorstCellsByKpiAndDistrict(String kpiName, String period, boolean excludeZeroes, String districtName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = dateService.getLatestDate(rat);

        if (dateService.isDateIsDay(timestamp, dateService.extractDayOfWeek(refreshDay))) {
            StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
            District district = districtService.findDistrictByName(districtName);

            LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

            LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);
            LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period));

            List<WorstCellSaveDto> dashboardWorstCells = kpiDayRepository.findWorstCellsForDashboardByDistrict(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, district.getId(), rat.getId(), excludeZeroes);
            return dashboardWorstCells.stream().map(wc -> createWorstCell(wc, period, districtName)).toList();
        } else return null;
    }
}
