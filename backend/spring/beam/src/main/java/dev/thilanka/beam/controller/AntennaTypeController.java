package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.AntennaTypeDto;
import dev.thilanka.beam.service.AntennaTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/antenna-types")
@RequiredArgsConstructor
public class AntennaTypeController {
    private final AntennaTypeService antennaTypeService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping
    public ResponseEntity<AntennaTypeDto> createAntennaType(@RequestBody @Valid AntennaTypeDto dto) {
        AntennaTypeDto saved = antennaTypeService.createAntennaType(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<AntennaTypeDto>> createAntennaTypeList(@RequestBody @Valid List<AntennaTypeDto> dtos) {
        List<AntennaTypeDto> saved = antennaTypeService.createAntennaTypes(dtos);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_UPDATE')")
    @PutMapping
    public ResponseEntity<AntennaTypeDto> updateAntennaType(@RequestBody @Valid AntennaTypeDto dto) {
        AntennaTypeDto updated = antennaTypeService.updateAntennaType(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_DELETE')")
    @DeleteMapping("{name}")
    public ResponseEntity<Void> deleteAntennaType(@PathVariable("name") String name) {
        antennaTypeService.deleteAntennaType(name);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{name}")
    public ResponseEntity<AntennaTypeDto> getByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(antennaTypeService.getByName(name));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<AntennaTypeDto>> getAll() {
        return ResponseEntity.ok(antennaTypeService.getAll());
    }
}
