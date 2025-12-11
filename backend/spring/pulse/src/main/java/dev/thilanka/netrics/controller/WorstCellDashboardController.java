package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.WorstCellSaveDto;
import dev.thilanka.netrics.dto.WorstCellsDashboardDto;
import dev.thilanka.netrics.dto.WorstCellsWithLatestDto;
import dev.thilanka.netrics.service.DateService;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.WorstCellDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pulse/worst-cell-dashboard")
@RequiredArgsConstructor
public class WorstCellDashboardController {
    private final WorstCellDashboardService worstCellDashboardService;
    private final DateService dateService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    public List<WorstCellSaveDto> createWorstCellsByKpiAndArea(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName,
            @RequestParam String granularityName) {
        return worstCellDashboardService.createWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, areaName, ratName,granularityName);
    }

    //--    Create WorstCells for Dashboard at a custom date
    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("custom")
    public List<WorstCellSaveDto> createWorstCellsByKpiAndAreaCustom(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String date,
            @RequestParam String ratName,
            @RequestParam String granularityName) {
        LocalDateTime timestamp = dateService.extractDate(date);
        return worstCellDashboardService.createWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, areaName, timestamp, ratName,granularityName);
    }

    //--    Create all WorstCells for Dashboard at a custom date by AreaType and RAT    String period, boolean excludeZeroes, String areaType, LocalDateTime timestamp, String ratName
    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("areatype-rat")
    public ResponseEntity<Map<String,List<WorstCellSaveDto>>> createWorstCellsByRatAndAreaType(
            @RequestParam String period,
            @RequestParam String areaType,
            @RequestParam String date,
            @RequestParam String ratName) {
        LocalDateTime timestamp = dateService.extractDate(date);
        Map<String,List<WorstCellSaveDto>> savedWorstCells = worstCellDashboardService.createWorstCellsByRatAndAreaType(period, areaType, timestamp, ratName);
        return new ResponseEntity<>(savedWorstCells, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public List<WorstCellsWithLatestDto> getWorstCellsByKpiAndArea(
            @RequestParam String timestamp,
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName,
            @RequestParam String granularityName
    ) {
        return worstCellDashboardService.getWorstCellsByKpiAndArea(timestamp, kpiName, period, excludeZeroes, areaName, ratName, granularityName);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("timestamps")
    public List<Timestamp> getTimestamps(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam String ratName
    ){
        return worstCellDashboardService.getTimestamps(kpiName,period,areaName,ratName);
    }

}
