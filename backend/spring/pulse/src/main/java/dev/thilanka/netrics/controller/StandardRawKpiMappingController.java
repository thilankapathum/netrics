package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import dev.thilanka.netrics.service.StandardRawKpiMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/standard-raw-kpimap")
@RequiredArgsConstructor
public class StandardRawKpiMappingController {
    private final StandardRawKpiMappingService standardRawKpiMappingService;

    @GetMapping
    ResponseEntity<List<StandardRawKpiMappingDto>> getAll(@RequestParam("ratName") String ratName){
        List<StandardRawKpiMappingDto> dtos = standardRawKpiMappingService.getAll(ratName);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    ResponseEntity<StandardRawKpiMappingDto> createLteFddStandardKpiMapping(@RequestBody @Valid StandardRawKpiMappingDto dto){
        StandardRawKpiMappingDto savedDto = standardRawKpiMappingService.createMapping(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    ResponseEntity<List<StandardRawKpiMappingDto>> createLteFddStandardKpiMappingList(@RequestBody @Valid List<StandardRawKpiMappingDto> dtos){
        List<StandardRawKpiMappingDto> savedDtos = standardRawKpiMappingService.createMappingList(dtos);
        return new ResponseEntity<>(savedDtos,HttpStatus.CREATED);
    }
}
