package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.MapCellThrSetAndThresholds;
import dev.thilanka.netrics.dto.MapCellThresholdDto;
import dev.thilanka.netrics.service.MapCellThresholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/map-cell-thresholds")
@RequiredArgsConstructor
public class MapCellThresholdController {
    private final MapCellThresholdService mapCellThresholdService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping
    public ResponseEntity<MapCellThresholdDto> createThreshold(@RequestBody @Valid MapCellThresholdDto dto) {
        MapCellThresholdDto savedThreshold = mapCellThresholdService.createThreshold(dto);
        return new ResponseEntity<>(savedThreshold, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("thr-set")
    public ResponseEntity<List<MapCellThresholdDto>> getThresholdsByThrSetId(@RequestParam("id") Long id) {
        List<MapCellThresholdDto> thresholds = mapCellThresholdService.getThresholdsByThrSetId(id);
        return new ResponseEntity<>(thresholds, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("thr-set-thresholds")
    public ResponseEntity<MapCellThrSetAndThresholds> getThrSetAndThresholdsByThrSetId(@RequestParam("id") Long id) {
        MapCellThrSetAndThresholds thresholds = mapCellThresholdService.getThrSetAndThresholdsByThrSetId(id);
        return new ResponseEntity<>(thresholds, HttpStatus.OK);
    }
}
