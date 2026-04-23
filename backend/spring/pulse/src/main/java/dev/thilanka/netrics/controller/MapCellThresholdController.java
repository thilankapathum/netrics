package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.service.MapCellThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/map-cell-thresholds")
@RequiredArgsConstructor
public class MapCellThresholdController {
    private final MapCellThresholdService mapCellThresholdService;
}
