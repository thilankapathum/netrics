package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.WorstCell;
import dev.thilanka.netrics.entity.KpiSnapshot;
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
    public ResponseEntity<List<KpiSnapshot>> getCalculatedBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period){
        return ResponseEntity.ok(lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshots(kpiName, period));
    }

    @GetMapping("worst-cells")
    public ResponseEntity<List<WorstCell>> getWorstCellsByKpi(@RequestParam String kpiName, @RequestParam String period, @RequestParam int count){
        return ResponseEntity.ok(lteFddKpiDayService.getWorstCellsByKpi(kpiName, period,count));
    }
}
