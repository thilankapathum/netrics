package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.StandardKpiDto;
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
    public ResponseEntity<List<StandardKpiDto>> getAllLteFddStandardKpi() {
        List<StandardKpiDto> standardKpiDtos = lteFddStandardKpiService.getAll();
        return ResponseEntity.ok(standardKpiDtos);
    }

    @PostMapping
    public ResponseEntity<StandardKpiDto> createLteFddStandardKpi(@RequestBody @Valid StandardKpiDto dto) {
        StandardKpiDto savedDto = lteFddStandardKpiService.createKpi(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("/list")
    public ResponseEntity<List<StandardKpiDto>> createLteFddStandardKpis(@RequestBody @Valid List<StandardKpiDto> dtos) {
        List<StandardKpiDto> savedDtos = lteFddStandardKpiService.createKpis(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

}
