package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import dev.thilanka.netrics.service.StandardRawKpiMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/standard-raw-kpimap")
@RequiredArgsConstructor
public class StandardRawKpiMappingController {
    private final StandardRawKpiMappingService standardRawKpiMappingService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping
    ResponseEntity<List<StandardRawKpiMappingDto>> getAll(@RequestParam("ratName") String ratName){
        List<StandardRawKpiMappingDto> dtos = standardRawKpiMappingService.getAll(ratName);
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    ResponseEntity<StandardRawKpiMappingDto> createStandardKpiMapping(@RequestBody @Valid StandardRawKpiMappingDto dto){
        StandardRawKpiMappingDto savedDto = standardRawKpiMappingService.createMapping(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("list")
    ResponseEntity<List<StandardRawKpiMappingDto>> createStandardKpiMappingList(@RequestBody @Valid List<StandardRawKpiMappingDto> dtos){
        List<StandardRawKpiMappingDto> savedDtos = standardRawKpiMappingService.createMappingList(dtos);
        return new ResponseEntity<>(savedDtos,HttpStatus.CREATED);
    }
}
