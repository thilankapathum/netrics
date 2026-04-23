package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.MapCellThrSetDto;
import dev.thilanka.netrics.service.MapCellThrSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/map-cell-thr-sets")
@RequiredArgsConstructor
public class MapCellThrSetController {
    private final MapCellThrSetService mapCellThrSetService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping
    public ResponseEntity<MapCellThrSetDto> createThrSet(@RequestBody @Valid MapCellThrSetDto dto) {
        MapCellThrSetDto savedThrSet = mapCellThrSetService.createThrSet(dto);
        return new ResponseEntity<>(savedThrSet, HttpStatus.CREATED);
    }
}
