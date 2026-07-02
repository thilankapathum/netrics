package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiAnomalyDto;
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
}
