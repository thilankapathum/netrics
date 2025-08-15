package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddStandardKpiDto;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/standardkpi")
@RequiredArgsConstructor
public class LteFddStandardKpiController {
    private final LteFddStandardKpiService lteFddStandardKpiService;

    @GetMapping
    public ResponseEntity<List<LteFddStandardKpiDto>> getAllLteFddStandardKpi() {
        List<LteFddStandardKpiDto> lteFddStandardKpiDtos = lteFddStandardKpiService.getAll();
        return ResponseEntity.ok(lteFddStandardKpiDtos);
    }

    @PostMapping
    public ResponseEntity<LteFddStandardKpiDto> createLteFddStandardKpi(@RequestBody @Valid LteFddStandardKpiDto dto) {
        LteFddStandardKpiDto savedDto = lteFddStandardKpiService.createKpi(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("/list")
    public ResponseEntity<List<LteFddStandardKpiDto>> createLteFddStandardKpis(@RequestBody @Valid List<LteFddStandardKpiDto> dtos) {
        List<LteFddStandardKpiDto> savedDtos = lteFddStandardKpiService.createKpis(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

}
