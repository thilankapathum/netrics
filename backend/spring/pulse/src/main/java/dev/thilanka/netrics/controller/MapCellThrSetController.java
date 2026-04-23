package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.MapCellThrSetResponseDto;
import dev.thilanka.netrics.service.MapCellThrSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pulse/map-cell-thr-sets")
@RequiredArgsConstructor
public class MapCellThrSetController {
    private final MapCellThrSetService mapCellThrSetService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping
    public ResponseEntity<MapCellThrSetResponseDto> createThrSet(@RequestBody @Valid MapCellThrSetResponseDto dto) {
        MapCellThrSetResponseDto savedThrSet = mapCellThrSetService.createThrSet(dto);
        return new ResponseEntity<>(savedThrSet, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<MapCellThrSetResponseDto> getThrSetResponse(
            @RequestParam("standardKpiName") String standardKpiName,
            @RequestParam("ratName") String ratName,
            @RequestParam("granularityName") String granularityName,
            @RequestParam("isAdmin") boolean isAdmin){
        return ResponseEntity.ok(mapCellThrSetService.getThrSetResponse(standardKpiName, ratName, granularityName, isAdmin));
    }
}