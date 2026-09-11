package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.AntennaDto;
import dev.thilanka.beam.service.AntennaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/antennas")
@RequiredArgsConstructor
public class AntennaController {
    private final AntennaService antennaService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping
    public ResponseEntity<AntennaDto> createAntenna(@RequestBody @Valid AntennaDto dto) {
        AntennaDto saved = antennaService.createAntenna(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<AntennaDto>> createAntennaList(@RequestBody @Valid List<AntennaDto> dtos) {
        List<AntennaDto> saved = antennaService.createAntennas(dtos);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_UPDATE')")
    @PutMapping
    public ResponseEntity<AntennaDto> updateAntenna(@RequestBody @Valid AntennaDto dto) {
        AntennaDto updated = antennaService.updateAntenna(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_DELETE')")
    @DeleteMapping
    public ResponseEntity<AntennaDto> deleteAntenna(@RequestBody @Valid AntennaDto dto) {
        AntennaDto deleted = antennaService.deleteAntenna(dto);
        return ResponseEntity.ok(deleted);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{id}")
    public ResponseEntity<AntennaDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(antennaService.getById(id));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<AntennaDto>> getBySectorName(@RequestParam("sectorName") String sectorName) {
        return ResponseEntity.ok(antennaService.getBySectorName(sectorName));
    }
}
