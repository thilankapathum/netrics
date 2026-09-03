package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellMappingDto;
import dev.thilanka.netrics.service.CellMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/cell-mappings")
@RequiredArgsConstructor
public class CellMappingController {
    private final CellMappingService cellMappingService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    public ResponseEntity<CellMappingDto> createCellMapping(@RequestBody @Valid CellMappingDto dto) {
        CellMappingDto saved = cellMappingService.createCellMapping(dto.previousCellName(), dto.newCellName());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<List<CellMappingDto>> getAll() {
        return ResponseEntity.ok(cellMappingService.getAll());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCellMapping(@PathVariable Long id) {
        cellMappingService.deleteCellMapping(id);
        return ResponseEntity.noContent().build();
    }
}
