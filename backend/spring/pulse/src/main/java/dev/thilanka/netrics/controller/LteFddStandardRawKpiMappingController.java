package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import dev.thilanka.netrics.service.LteFddStandardRawKpiMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/standard-raw-kpimap")
@RequiredArgsConstructor
public class LteFddStandardRawKpiMappingController {
    private final LteFddStandardRawKpiMappingService lteFddStandardRawKpiMappingService;

    @GetMapping
    ResponseEntity<List<StandardRawKpiMappingDto>> getAll(@RequestParam("ratName") String ratName){
        List<StandardRawKpiMappingDto> dtos = lteFddStandardRawKpiMappingService.getAll(ratName);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    ResponseEntity<StandardRawKpiMappingDto> createLteFddStandardKpiMapping(@RequestBody @Valid StandardRawKpiMappingDto dto){
        StandardRawKpiMappingDto savedDto = lteFddStandardRawKpiMappingService.createMapping(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    ResponseEntity<List<StandardRawKpiMappingDto>> createLteFddStandardKpiMappingList(@RequestBody @Valid List<StandardRawKpiMappingDto> dtos){
        List<StandardRawKpiMappingDto> savedDtos = lteFddStandardRawKpiMappingService.createMappingList(dtos);
        return new ResponseEntity<>(savedDtos,HttpStatus.CREATED);
    }
}
