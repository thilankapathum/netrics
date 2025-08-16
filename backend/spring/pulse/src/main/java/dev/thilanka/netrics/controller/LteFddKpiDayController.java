package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.KpiSnapshot;
import dev.thilanka.netrics.entity.FinalKpiSnapshot;
import dev.thilanka.netrics.entity.WorstCellKpiData;
import dev.thilanka.netrics.entity.WorstCellKpiDataCurrPre;
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

    @GetMapping("snapshot/calculated")
    public ResponseEntity<KpiSnapshot> getCalculatedKpiSnapshot(@RequestParam String kpiName, @RequestParam String period){
        return ResponseEntity.ok(lteFddKpiDayService.getLatestCalculatedKpiSnapshot(kpiName, period,false));
    }

    @GetMapping("snapshot/basic-kpi")
    public ResponseEntity<List<FinalKpiSnapshot>> getCalculatedBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period){
        return ResponseEntity.ok(lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshot(kpiName, period));
    }

    @GetMapping("worst-cells")
    public ResponseEntity<List<WorstCellKpiData>> getWorstCells(@RequestParam String kpiName, @RequestParam String period, @RequestParam int count){
        return ResponseEntity.ok(lteFddKpiDayService.findWorstCellsByKpi(kpiName, period,count));
    }

    @GetMapping("worst-cells-with-pre")
    public ResponseEntity<List<WorstCellKpiDataCurrPre>> getWorstCellsWithPre(@RequestParam String kpiName, @RequestParam String period, @RequestParam int count){
        return ResponseEntity.ok(lteFddKpiDayService.getWorstCellsByKpiWithPre(kpiName, period,count));
    }

    @GetMapping("worst-cells/final")
    public ResponseEntity<List<FinalKpiSnapshot>> getWorstCellsWithPreFinal(@RequestParam String kpiName, @RequestParam String period, @RequestParam int count,@RequestParam boolean isBasic){
        return ResponseEntity.ok(lteFddKpiDayService.getFinalWorstCellsByKpi(kpiName, period,count,isBasic));
    }
}
