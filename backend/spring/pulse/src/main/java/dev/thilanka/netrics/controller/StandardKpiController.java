package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.service.StandardKpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/standardkpi")
@RequiredArgsConstructor
public class StandardKpiController {
    private final StandardKpiService standardKpiService;

    @GetMapping
    public ResponseEntity<List<StandardKpiDto>> getAllStandardKpiByRat(@RequestParam("ratName") String ratName) {
        List<StandardKpiDto> standardKpiDtos = standardKpiService.getAllStandardKpiByRat(ratName);
        return ResponseEntity.ok(standardKpiDtos);
    }

    @PostMapping
    public ResponseEntity<StandardKpiDto> createLteFddStandardKpi(@RequestBody @Valid StandardKpiDto dto) {
        StandardKpiDto savedDto = standardKpiService.createKpi(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("/list")
    public ResponseEntity<List<StandardKpiDto>> createLteFddStandardKpis(@RequestBody @Valid List<StandardKpiDto> dtos) {
        List<StandardKpiDto> savedDtos = standardKpiService.createKpis(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

}
