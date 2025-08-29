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
    public ResponseEntity<BasicKpiSnapshot> getCalculatedBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period) {
        return ResponseEntity.ok(lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshots(kpiName, period));
    }

//    @GetMapping("worst-cells")
//    public ResponseEntity<List<WorstCell>> getWorstCellsByKpi(@RequestParam String kpiName, @RequestParam String period, @RequestParam int count){
//        return ResponseEntity.ok(lteFddKpiDayService.getWorstCellsByKpi(kpiName, period,count));
//    }

//    @GetMapping("worst-cells-page")
//    public Page<WorstCellsDto> getWorstCellsByKpiPage(
//            @RequestParam String kpiName,
//            @RequestParam String period,
//            @RequestParam int page,
//            @RequestParam int size) {
//
//        return lteFddKpiDayService.getWorstCellsByKpiPage(kpiName, period, page, size);
//
//    }

    @GetMapping("worst-cells-page")
    public Page<WorstCellsDto> getWorstCellsByKpiAndDistrictPage(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String districtName,
            @RequestParam int page,
            @RequestParam int size) {

        if (Objects.equals(districtName, "All Districts") || districtName == null){
            return lteFddKpiDayService.getWorstCellsByKpiPage(kpiName, period, page, size);
        } else {
            return lteFddKpiDayService.getWorstCellsByKpiAndDistrictPage(kpiName, period, districtName, page, size);
        }
    }

    @GetMapping("cell")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiAndCell(@RequestParam String kpiName, @RequestParam String cellName, @RequestParam String period) {
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.getDataByKpiAndCell(kpiName, cellName, period);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("cell-label")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiLabelAndCell(@RequestParam String kpiLabel, @RequestParam String cellName, @RequestParam String period) {
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.getDataByKpiLabelAndCell(kpiLabel, cellName, period);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("kpi")
    public ResponseEntity<List<KpiTrendDto>> getTrendDataByKpi(@RequestParam String kpiName, @RequestParam String period) {
        List<KpiTrendDto> kpiTrendDtos = lteFddKpiDayService.getTrendByKpi(kpiName, period);
        return ResponseEntity.ok(kpiTrendDtos);
    }
}
