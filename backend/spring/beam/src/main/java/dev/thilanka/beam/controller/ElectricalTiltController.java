package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.ElectricalTiltDto;
import dev.thilanka.beam.service.ElectricalTiltService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/electrical-tilts")
@RequiredArgsConstructor
public class ElectricalTiltController {
    private final ElectricalTiltService electricalTiltService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping
    public ResponseEntity<ElectricalTiltDto> createElectricalTilt(@RequestBody @Valid ElectricalTiltDto dto) {
        ElectricalTiltDto saved = electricalTiltService.createElectricalTilt(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<ElectricalTiltDto>> createElectricalTiltList(@RequestBody @Valid List<ElectricalTiltDto> dtos) {
        List<ElectricalTiltDto> saved = electricalTiltService.createElectricalTilts(dtos);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_UPDATE')")
    @PutMapping
    public ResponseEntity<ElectricalTiltDto> updateElectricalTilt(@RequestBody @Valid ElectricalTiltDto dto) {
        ElectricalTiltDto updated = electricalTiltService.updateElectricalTilt(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_DELETE')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteElectricalTilt(@PathVariable("id") Long id) {
        electricalTiltService.deleteElectricalTilt(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{id}")
    public ResponseEntity<ElectricalTiltDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(electricalTiltService.getById(id));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<ElectricalTiltDto>> getByAntennaId(@RequestParam("antennaId") Long antennaId) {
        return ResponseEntity.ok(electricalTiltService.getByAntennaId(antennaId));
    }
}
