package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.DashboardWorstCellDto;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.WorstCellDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/pulse/worst-cell-dashboard")
@RequiredArgsConstructor
public class WorstCellDashboardController {
    private final WorstCellDashboardService worstCellDashboardService;
    private final KpiDayService kpiDayService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public List<DashboardWorstCellDto> getWorstCellsByKpiAndDistrictPage(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String districtName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName) {

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            return worstCellDashboardService.createWorstCellsByKpi(kpiName, period,"All Districts",excludeZeroes,ratName);
        } else {
//            return worstCellDashboardService.getWorstCellsByKpiAndDistrict(kpiName, period, excludeZeroes,districtName, ratName);
            return worstCellDashboardService.createWorstCellsByKpiAndDistrict(kpiName,period,excludeZeroes,districtName,ratName);
//            return null;
        }
    }
}
