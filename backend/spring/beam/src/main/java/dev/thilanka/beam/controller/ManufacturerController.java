package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.ManufacturerDto;
import dev.thilanka.beam.service.ManufacturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {
    private final ManufacturerService manufacturerService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping
    public ResponseEntity<ManufacturerDto> createManufacturer(@RequestBody @Valid ManufacturerDto dto) {
        ManufacturerDto saved = manufacturerService.createManufacturer(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<ManufacturerDto>> createManufacturerList(@RequestBody @Valid List<ManufacturerDto> dtos) {
        List<ManufacturerDto> saved = manufacturerService.createManufacturers(dtos);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_UPDATE')")
    @PutMapping
    public ResponseEntity<ManufacturerDto> updateManufacturer(@RequestBody @Valid ManufacturerDto dto) {
        ManufacturerDto updated = manufacturerService.updateManufacturer(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_DELETE')")
    @DeleteMapping("{name}")
    public ResponseEntity<ManufacturerDto> deleteManufacturer(@PathVariable("name") String name,
                                                                @RequestParam(defaultValue = "false") boolean force) {
        ManufacturerDto deleted = manufacturerService.deleteManufacturerDto(name, force);
        return ResponseEntity.ok(deleted);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{name}/dependents")
    public ResponseEntity<List<String>> getDependents(@PathVariable("name") String name) {
        return ResponseEntity.ok(manufacturerService.getDependentAntennaLabels(name));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{name}")
    public ResponseEntity<ManufacturerDto> getByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(manufacturerService.getByName(name));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<ManufacturerDto>> getAll() {
        return ResponseEntity.ok(manufacturerService.getAll());
    }
}
