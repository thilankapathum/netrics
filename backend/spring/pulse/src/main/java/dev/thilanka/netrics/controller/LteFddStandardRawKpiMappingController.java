package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddStandardRawKpiMappingDto;
import dev.thilanka.netrics.service.LteFddStandardRawKpiMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd-standard-raw-kpimap")
@RequiredArgsConstructor
public class LteFddStandardRawKpiMappingController {
    private final LteFddStandardRawKpiMappingService lteFddStandardRawKpiMappingService;

    @GetMapping
    ResponseEntity<List<LteFddStandardRawKpiMappingDto>> getAll(){
        List<LteFddStandardRawKpiMappingDto> dtos = lteFddStandardRawKpiMappingService.getAll();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    ResponseEntity<LteFddStandardRawKpiMappingDto> createLteFddStandardKpiMapping(@RequestBody @Valid LteFddStandardRawKpiMappingDto dto){
        LteFddStandardRawKpiMappingDto savedDto = lteFddStandardRawKpiMappingService.createMapping(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    ResponseEntity<List<LteFddStandardRawKpiMappingDto>> createLteFddStandardKpiMappingList(@RequestBody @Valid List<LteFddStandardRawKpiMappingDto> dtos){
        List<LteFddStandardRawKpiMappingDto> savedDtos = lteFddStandardRawKpiMappingService.createMappingList(dtos);
        return new ResponseEntity<>(savedDtos,HttpStatus.CREATED);
    }
}
