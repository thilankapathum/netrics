package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.BasicKpiSnapshot;
import dev.thilanka.netrics.service.KpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/pulse/kpiday")
@RequiredArgsConstructor
public class KpiDayController {
    private final KpiDayService kpiDayService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @GetMapping
    public ResponseEntity<List<KpiDataDto>> getAll() {
        List<KpiDataDto> kpiDataDtos = kpiDayService.findAll();
        return ResponseEntity.ok(kpiDataDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("snapshot/basic-kpi")
    public ResponseEntity<BasicKpiSnapshot> getCalculatedBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period, @RequestParam String districtName, @RequestParam String ratName) {

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            return ResponseEntity.ok(kpiDayService.getLatestBasicAndStandardKpiSnapshots(kpiName, period, ratName));
        } else {
            return ResponseEntity.ok(kpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(kpiName, period, districtName, ratName));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("worst-cells")
    public List<WorstCellsDto> getWorstCellsByKpiAndDistrictPage(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String districtName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName) {

        if (Objects.equals(districtName, "All Districts") || districtName == null){
//            if (excludeZeroes){
//                return kpiDayService.getWorstCellsByKpiExcludeZeroes(kpiName, period, ratName);
//            } else return kpiDayService.getWorstCellsByKpi(kpiName, period,ratName);
            return kpiDayService.getWorstCellsByKpi(kpiName, period,excludeZeroes,ratName);
        } else {
//            if (excludeZeroes) {
//                return kpiDayService.getWorstCellsByKpiAndDistrictExcludeZeroes(kpiName, period, districtName, ratName);
//            } else return kpiDayService.getWorstCellsByKpiAndDistrict(kpiName, period, districtName, ratName);
            return kpiDayService.getWorstCellsByKpiAndDistrict(kpiName, period, excludeZeroes,districtName, ratName);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("cell")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiAndCell(@RequestParam String kpiName, @RequestParam String cellName, @RequestParam String period, @RequestParam String ratName) {
        List<KpiDataDto> kpiDataDtos = kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("cell-label")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiLabelAndCell(@RequestParam String kpiLabel, @RequestParam String cellName, @RequestParam String period, @RequestParam String ratName) {
        List<KpiDataDto> kpiDataDtos = kpiDayService.getDataByKpiLabelAndCell(kpiLabel, cellName, period, ratName);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("kpi")
    public ResponseEntity<List<KpiTrendDto>> getTrendDataByKpi(@RequestParam String kpiName, @RequestParam String period, @RequestParam String districtName, @RequestParam String ratName) {
        List<KpiTrendDto> kpiTrendDtos = new ArrayList<>();

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            kpiTrendDtos = kpiDayService.getTrendByKpi(kpiName, period, ratName);
        } else {
            kpiTrendDtos = kpiDayService.getTrendByKpiAndDistrict(kpiName, period, districtName, ratName);
        }

        return ResponseEntity.ok(kpiTrendDtos);
    }
}
