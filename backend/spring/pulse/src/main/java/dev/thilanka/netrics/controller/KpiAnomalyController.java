package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AnomalySummaryDto;
import dev.thilanka.netrics.dto.KpiAnomalyDto;
import dev.thilanka.netrics.dto.PagedResponse;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.service.AnomalyDetectionService;
import dev.thilanka.netrics.service.KpiAnomalyService;
import dev.thilanka.netrics.service.impl.AnomalyDetectionScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/anomaly")
@RequiredArgsConstructor
public class KpiAnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;
    private final AnomalyDetectionScheduler anomalyDetectionScheduler;
    private final KpiAnomalyService kpiAnomalyService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<KpiAnomalyDto>> getLatestAnomalies(
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam(required = false) String minSeverity) {
        return ResponseEntity.ok(kpiAnomalyService.getLatestAnomalies(ratName, granularityName, minSeverity));
    }

    @GetMapping("run")
    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    public ResponseEntity<String> runAnomalyDetection() {
        anomalyDetectionScheduler.runAnomalyDetection();
        return ResponseEntity.ok("Ran Anomaly detection");
    }

    @GetMapping("run/custom")
    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    public ResponseEntity<String> runAnomalyDetection(@RequestParam String granularityName,
                                                      @RequestParam String ratName) {
        anomalyDetectionScheduler.runAnomalyDetection(granularityName, ratName);
        return ResponseEntity.ok("Ran Anomaly detection");
    }

    @GetMapping("by-area")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<WorstCellsDto>> getAllAnomalyCells(
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName) {
        return ResponseEntity.ok(kpiAnomalyService.getAllAnomalyCellsByArea(period, areaName, ratName, granularityName));
    }

    @GetMapping("cells")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<PagedResponse<WorstCellsDto>> getAllAnomalyCells(
            @RequestParam String period,
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String kpiName,
            @RequestParam(defaultValue = "severity") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(kpiAnomalyService.getAllAnomalyCellsByArea(
                period, areaName, ratName, granularityName, severity, kpiName, sortBy, sortDir, page, pageSize));
    }

    @GetMapping("summary")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<AnomalySummaryDto> getAnomalySummary(
            @RequestParam String areaName,
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam(required = false) String kpiName) {
        return ResponseEntity.ok(kpiAnomalyService.getAnomalySummaryByArea(areaName, ratName, granularityName, kpiName));
    }
}
