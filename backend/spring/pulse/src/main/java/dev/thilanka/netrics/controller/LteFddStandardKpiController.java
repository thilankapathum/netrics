package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddStandardKpiDto;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefddstandardkpi")
@RequiredArgsConstructor
public class LteFddStandardKpiController {
    private final LteFddStandardKpiService lteFddStandardKpiService;

    @GetMapping
    public ResponseEntity<List<LteFddStandardKpiDto>> getAllLteFddStandardKpi(){
        List<LteFddStandardKpiDto> lteFddStandardKpiDtos = lteFddStandardKpiService.getAll();
        return ResponseEntity.ok(lteFddStandardKpiDtos);
    }

    @PostMapping
    public ResponseEntity<LteFddStandardKpiDto> createLteFddStandardKpi(@RequestBody @Valid LteFddStandardKpiDto lteFddStandardKpiDto){
        LteFddStandardKpiDto savedLteFddStandardKpiDto = lteFddStandardKpiService.createLteFddStandardKpi(lteFddStandardKpiDto);
        return new ResponseEntity<>(savedLteFddStandardKpiDto, HttpStatus.CREATED);
    }

}
