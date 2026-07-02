package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.service.AnomalyDetectionService;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.impl.AnomalyDetectionScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/anomaly")
@RequiredArgsConstructor
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;
    private final AnomalyDetectionScheduler anomalyDetectionScheduler;

    @GetMapping
    public ResponseEntity<String> runAnomalyDetection() {
//        anomalyDetectionService.runAnomalyDetection();
        anomalyDetectionScheduler.runAnomalyDetection();
        return ResponseEntity.ok("Ran Anomaly detection");
    }
}
