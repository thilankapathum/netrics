package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.DashboardWorstCellDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
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

    @Value("${app.worst-cell-dashboard.refresh-day}")
    private String refreshDay;

    @Override
    public DashboardWorstCellDto createWorstCell(DashboardWorstCellDto dashboardWorstCell, String period, String areaAggregation) {
        WorstCell worstCell = mapper.dashboardWorstCellDtoToWorstCell(dashboardWorstCell, period, areaAggregation);
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
                    worstCell.getStandardKpi().getId()
            ).orElseThrow(() -> new RuntimeException("Worst cell query error!"));

            return mapper.toDashboardWorstCellDto(existingWorstCell);
        }
    }

    @Override
    public List<DashboardWorstCellDto> createWorstCellsByKpi(String kpiName, String period, String areaAggregation, boolean excludeZeroes, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = dateService.getLatestDate(rat);

        if (dateService.isDateIsDay(timestamp, dateService.extractDayOfWeek(refreshDay))) {

            StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
            LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

            LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);
            LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period));

            List<DashboardWorstCellDto> dashboardWorstCells = kpiDayRepository.findWorstCellsForDashboard(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, rat.getId(), excludeZeroes);
            return dashboardWorstCells.stream().map(wc -> createWorstCell(wc, period, areaAggregation)).toList();
        } else return null;
    }

    @Override
    public List<DashboardWorstCellDto> createWorstCellsByKpiAndDistrict(String kpiName, String period,boolean excludeZeroes, String districtName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = dateService.getLatestDate(rat);

        if (dateService.isDateIsDay(timestamp, dateService.extractDayOfWeek(refreshDay))) {
            StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
            District district = districtService.findDistrictByName(districtName);

            LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

            LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);
            LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period));

            List<DashboardWorstCellDto> dashboardWorstCells = kpiDayRepository.findWorstCellsForDashboardByDistrict(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, district.getId(), rat.getId(), excludeZeroes);
            return dashboardWorstCells.stream().map(wc -> createWorstCell(wc,period, districtName)).toList();
//            return kpiDayRepository.findWorstCellsByDistrict(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, district.getId(), rat.getId(), excludeZeroes);
        } else return null;
    }
}
