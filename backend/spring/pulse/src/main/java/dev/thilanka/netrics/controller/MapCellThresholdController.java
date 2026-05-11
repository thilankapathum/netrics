package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.common.exception.BusinessValidationException;
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
    @GetMapping("templates/id/{id}")
    public ResponseEntity<MapCellThrSetAndThresholds> getThrSetAndThresholdsByThrSetId(@PathVariable("id") Long id) {
        MapCellThrSetAndThresholds thresholds = mapCellThresholdService.getThrSetAndThresholdsByThrSetId(id);
        return new ResponseEntity<>(thresholds, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("templates")
    public ResponseEntity<MapCellThrSetAndThresholds> getThrSetAndThresholds(
            @RequestParam("standardKpiName") String standardKpiName,
            @RequestParam("granularityName") String granularityName,
            @RequestParam("ratName") String ratName,
            @RequestParam("isAdmin") boolean isAdmin) {
        MapCellThrSetAndThresholds thresholds = mapCellThresholdService.getThrSetAndThresholds(standardKpiName, ratName, granularityName, isAdmin);
        return ResponseEntity.ok(thresholds);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping("templates")
    public ResponseEntity<MapCellThrSetAndThresholds> createThrSetAndThresholds(@RequestBody @Valid MapCellThrSetAndThresholds dto) {
        if (dto.thrSet().id() == 0) {
            MapCellThrSetAndThresholds savedThrSetAndThresholds = mapCellThresholdService.createMapCellThrSetAndThresholds(dto);
            return new ResponseEntity<>(savedThrSetAndThresholds, HttpStatus.CREATED);
        } else {
            throw new BusinessValidationException("Threshold set already exists");
        }
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PutMapping("templates")
    public ResponseEntity<MapCellThrSetAndThresholds> updateThresholds(@RequestBody @Valid MapCellThrSetAndThresholds dto) {
        if (dto.thrSet().id() != 0) {
            MapCellThrSetAndThresholds updated = mapCellThresholdService.updateMapCellThrSetAndThresholds(dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            throw new BusinessValidationException("Request invalid for updating Thresholds");
        }
    }
}
