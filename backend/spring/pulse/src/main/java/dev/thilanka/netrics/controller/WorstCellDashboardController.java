package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.DashboardWorstCellDto;
import dev.thilanka.netrics.service.DateService;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.WorstCellDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/pulse/worst-cell-dashboard")
@RequiredArgsConstructor
public class WorstCellDashboardController {
    private final WorstCellDashboardService worstCellDashboardService;
    private final KpiDayService kpiDayService;
    private final DateService dateService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PostMapping
    public List<DashboardWorstCellDto> createWorstCellsByKpiAndArea(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName) {
        return worstCellDashboardService.createWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, areaName, ratName);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PostMapping("custom")
    public List<DashboardWorstCellDto> createWorstCellsByKpiAndAreaCustom(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String date,
            @RequestParam String ratName) {
        LocalDateTime timestamp = dateService.extractDate(date);
        return worstCellDashboardService.createWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, areaName, timestamp, ratName);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public List<DashboardWorstCellDto> getWorstCellsByKpiAndArea(
            @RequestParam String timestamp,
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName
    ) {
        return worstCellDashboardService.getWorstCellsByKpiAndArea(timestamp, kpiName, period, excludeZeroes, areaName, ratName);
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
