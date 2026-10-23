package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.InfraTypeDto;
import dev.thilanka.beam.service.InfraTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/infra-types")
@RequiredArgsConstructor
public class InfraTypeController {
    private final InfraTypeService infraTypeService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping
    public ResponseEntity<InfraTypeDto> createInfraType(@RequestBody @Valid InfraTypeDto dto) {
        InfraTypeDto saved = infraTypeService.createInfraType(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<InfraTypeDto>> createInfraTypeList(@RequestBody @Valid List<InfraTypeDto> dtos) {
        List<InfraTypeDto> saved = infraTypeService.createInfraTypes(dtos);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_UPDATE')")
    @PutMapping
    public ResponseEntity<InfraTypeDto> updateInfraType(@RequestBody @Valid InfraTypeDto dto) {
        InfraTypeDto updated = infraTypeService.updateInfraType(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{id}")
    public ResponseEntity<InfraTypeDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(infraTypeService.getById(id));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<InfraTypeDto>> getAllInfraTypes() {
        return ResponseEntity.ok(infraTypeService.getAllInfraTypes());
    }
}
