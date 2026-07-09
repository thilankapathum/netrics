package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.BasicKpiSnapshot;
import dev.thilanka.netrics.service.KpiDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/pulse/kpiday")
@RequiredArgsConstructor
@Slf4j
public class KpiDayController {
    private final KpiDayService kpiDayService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @GetMapping
    public ResponseEntity<List<KpiDataDto>> getAll() {
        List<KpiDataDto> kpiDataDtos = kpiDayService.findAll();
        return ResponseEntity.ok(kpiDataDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("snapshot/basic-kpi-area")
    public ResponseEntity<BasicKpiSnapshot> getBasicKpiSnapshot(@RequestParam String kpiName, @RequestParam String period, @RequestParam String areaName, @RequestParam String ratName, @RequestParam String granularityName) {
        return ResponseEntity.ok(kpiDayService.getLatestBasicAndStandardKpiSnapshotsByArea(kpiName, period, areaName, ratName, granularityName));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("worst-cells-area")
    public List<WorstCellsDto> getWorstCellsByKpiAndArea(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam boolean excludeZeroes,
            @RequestParam int limit,
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName) {

        return kpiDayService.getWorstCellsByKpiAndArea(kpiName, period, excludeZeroes, limit, areaName, ratName, granularityName);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("worst-cells-area-band")
    public List<WorstCellsDto> getWorstCellsByKpiAreaAndBand(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam boolean excludeZeroes,
            @RequestParam int limit,
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam String bandName) {

        return kpiDayService.getWorstCellsByKpiAreaAndBand(kpiName,period,excludeZeroes,limit,ratName,areaName,granularityName,bandName);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("cell")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiAndCell(@RequestParam String kpiName, @RequestParam String cellName, @RequestParam String period, @RequestParam String ratName, @RequestParam String granularityName) {
        List<KpiDataDto> kpiDataDtos = kpiDayService.getDataByKpiAndCell(kpiName, cellName, period, ratName, granularityName);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("cell-label")
    public ResponseEntity<List<KpiDataDto>> getDataByKpiLabelAndCell(@RequestParam String kpiLabel, @RequestParam String cellName, @RequestParam String period, @RequestParam String ratName, @RequestParam String granularityName) {
        List<KpiDataDto> kpiDataDtos = kpiDayService.getDataByKpiLabelAndCell(kpiLabel, cellName, period, ratName, granularityName);
        return ResponseEntity.ok(kpiDataDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("kpi-area")
    public ResponseEntity<List<KpiTrendDto>> getTrendDataByKpiAndArea(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName) {
        List<KpiTrendDto> kpiTrendDtos = kpiDayService.getTrendByKpiAndArea(kpiName,period,areaName,ratName,granularityName);
        return ResponseEntity.ok(kpiTrendDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("kpi-area-band")
    public ResponseEntity<List<KpiTrendDto>> getTrendDataByKpiAreaAndBand(
            @RequestParam String kpiName,
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam String bandName) {
        List<KpiTrendDto> kpiTrendDtos = kpiDayService.getTrendByKpiAreaAndBand(kpiName,period,areaName,ratName,granularityName,bandName);
        return ResponseEntity.ok(kpiTrendDtos);
    }

    @DeleteMapping("dedup-oss")
    public ResponseEntity<String> deduplicateLatestByOss(){
        try {
            int deleted = kpiDayService.deduplicateLatestByOss();
            return ResponseEntity.ok("Deduplication finished! " + deleted + " duplicate rows removed.");
        } catch (Exception ex) {
            log.error("Deduplication failed", ex);
            return ResponseEntity.internalServerError().body("Deduplication failed: " + ex.getMessage());
        }
    }
}
