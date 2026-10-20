package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.BeamBandDto;
import dev.thilanka.beam.service.BeamBandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/bands")
@RequiredArgsConstructor
public class BeamBandController {
    private final BeamBandService beamBandService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{name}")
    public ResponseEntity<BeamBandDto> getByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(beamBandService.getByName(name));
    }

    // Called by pulse (relayed JWT) before deleting a band, so pulse admins without BEAM_READ are allowed too
    @PreAuthorize("hasAnyAuthority('ROLE_BEAM_READ', 'ROLE_PULSE_READ')")
    @GetMapping("{name}/usage")
    public ResponseEntity<Long> getUsage(@PathVariable("name") String name) {
        return ResponseEntity.ok(beamBandService.countTilts(name));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<BeamBandDto>> getAll() {
        return ResponseEntity.ok(beamBandService.getAll());
    }
}
