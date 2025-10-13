package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.BasicKpiSnapshot;
import dev.thilanka.netrics.entity.WorstCell;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/kpiday")
@RequiredArgsConstructor
public class LteFddKpiDayController {
    private final LteFddKpiDayService lteFddKpiDayService;

    @GetMapping
    public ResponseEntity<List<KpiDataDto>> getAll() {
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.findAll();
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("snapshot/basic-kpi")
    public ResponseEntity<BasicKpiSnapshot> getCalculatedBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period, @RequestParam String districtName, @RequestParam String ratName) {

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            return ResponseEntity.ok(lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshots(kpiName, period, ratName));
        } else {
            return ResponseEntity.ok(lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(kpiName, period, districtName, ratName));
        }
    }

    @GetMapping("worst-cells")
    public List<WorstCellsDto> getWorstCellsByKpiAndDistrictPage(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String districtName,
            @RequestParam boolean excludeZeroes,
            @RequestParam String ratName) {

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            if (excludeZeroes){
                return lteFddKpiDayService.getWorstCellsByKpiExcludeZeroes(kpiName, period, ratName);
            } else return lteFddKpiDayService.getWorstCellsByKpi(kpiName, period,ratName);
        } else {
            if (excludeZeroes) {
                return lteFddKpiDayService.getWorstCellsByKpiAndDistrictExcludeZeroes(kpiName, period, districtName, ratName);
            } else return lteFddKpiDayService.getWorstCellsByKpiAndDistrict(kpiName, period, districtName, ratName);
        }
    }

    @GetMapping("cell")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiAndCell(@RequestParam String kpiName, @RequestParam String cellName, @RequestParam String period, @RequestParam String ratName) {
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("cell-label")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiLabelAndCell(@RequestParam String kpiLabel, @RequestParam String cellName, @RequestParam String period, @RequestParam String ratName) {
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.getDataByKpiLabelAndCell(kpiLabel, cellName, period, ratName);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("kpi")
    public ResponseEntity<List<KpiTrendDto>> getTrendDataByKpi(@RequestParam String kpiName, @RequestParam String period, @RequestParam String districtName, @RequestParam String ratName) {
        List<KpiTrendDto> kpiTrendDtos = new ArrayList<>();

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            kpiTrendDtos = lteFddKpiDayService.getTrendByKpi(kpiName, period, ratName);
        } else {
            kpiTrendDtos = lteFddKpiDayService.getTrendByKpiAndDistrict(kpiName, period, districtName, ratName);
        }

        return ResponseEntity.ok(kpiTrendDtos);
    }
}
