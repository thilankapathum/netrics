package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.entity.BasicKpiSnapshot;
import dev.thilanka.netrics.entity.WorstCell;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/kpiday")
@RequiredArgsConstructor
public class LteFddKpiDayController {
    private final LteFddKpiDayService lteFddKpiDayService;

    @GetMapping
    public ResponseEntity<List<KpiDataDto>> getAll(){
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.findAll();
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("snapshot/basic-kpi")
    public ResponseEntity<BasicKpiSnapshot> getCalculatedBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period){
        return ResponseEntity.ok(lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshots(kpiName, period));
    }

    @GetMapping("worst-cells")
    public ResponseEntity<List<WorstCell>> getWorstCellsByKpi(@RequestParam String kpiName, @RequestParam String period, @RequestParam int count){
        return ResponseEntity.ok(lteFddKpiDayService.getWorstCellsByKpi(kpiName, period,count));
    }

    @GetMapping("cell")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiAndCell(@RequestParam String kpiName, @RequestParam String cellName, @RequestParam String period){
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.getDataByKpiAndCell(kpiName, cellName, period);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("kpi")
    public ResponseEntity<List<KpiTrendDto>> getTrendDataByKpi(@RequestParam String kpiName, @RequestParam String period){
        List<KpiTrendDto> kpiTrendDtos = lteFddKpiDayService.getTrendByKpi(kpiName, period);
        return ResponseEntity.ok(kpiTrendDtos);
    }
}
