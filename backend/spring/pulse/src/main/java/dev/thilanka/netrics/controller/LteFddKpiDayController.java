package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
import dev.thilanka.netrics.entity.ltefdd.BasicStandardKpiData;
import dev.thilanka.netrics.entity.ltefdd.CompactKpiSnapshot;
import dev.thilanka.netrics.entity.ltefdd.KpiSnapshot;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefddkpiday")
@RequiredArgsConstructor
public class LteFddKpiDayController {
    private final LteFddKpiDayService lteFddKpiDayService;

    @GetMapping
    public ResponseEntity<List<KpiDataDto>> getAll(){
        List<KpiDataDto> kpiDataDtos = lteFddKpiDayService.findAll();
        return ResponseEntity.ok(kpiDataDtos);
    }

    @GetMapping("fractions")
    public  ResponseEntity<List<KpiDataFractionDto>> getAllWithFractions(){
        List<KpiDataFractionDto> list = lteFddKpiDayService.findAllWithFractions();
        return ResponseEntity.ok(list);
    }

    @GetMapping("snapshot/compact")
    public ResponseEntity<List<CompactKpiSnapshot>> getBasicSnapshot(@RequestParam String kpiName, @RequestParam String period){
        return ResponseEntity.ok(lteFddKpiDayService.getCompactBasicStandardKpiSnapshot(kpiName, period));
    }
}
