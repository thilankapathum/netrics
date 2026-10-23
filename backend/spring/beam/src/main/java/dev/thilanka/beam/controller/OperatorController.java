package dev.thilanka.beam.controller;

import dev.thilanka.beam.dto.OperatorDto;
import dev.thilanka.beam.service.OperatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beam/operators")
@RequiredArgsConstructor
public class OperatorController {
    private final OperatorService operatorService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping
    public ResponseEntity<OperatorDto> createOperator(@RequestBody @Valid OperatorDto dto) {
        OperatorDto saved = operatorService.createOperator(dto);
        return new  ResponseEntity<>(saved,HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<OperatorDto>> createOperators(@RequestBody @Valid List<OperatorDto> dtos) {
        List<OperatorDto> saved = operatorService.createOperators(dtos);
        return new  ResponseEntity<>(saved,HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping
    public ResponseEntity<List<OperatorDto>> getAllOperators() {
        return ResponseEntity.ok(operatorService.getAll());
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_UPDATE')")
    @PutMapping
    public ResponseEntity<OperatorDto> updateOperator(@RequestBody @Valid OperatorDto dto) {
        OperatorDto updated = operatorService.updateOperator(dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_DELETE')")
    @DeleteMapping("{name}")
    public ResponseEntity<Void> deleteOperator(@PathVariable("name") String name,
                                                @RequestParam(defaultValue = "false") boolean force) {
        operatorService.deleteOperator(name, force);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{name}/dependents")
    public ResponseEntity<List<String>> getDependents(@PathVariable("name") String name) {
        return ResponseEntity.ok(operatorService.getDependentSiteLabels(name));
    }

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("{name}")
    public ResponseEntity<OperatorDto> getByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(operatorService.findByOperatorName(name));
    }
}
