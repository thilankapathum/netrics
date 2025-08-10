package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddBasicKpiDto;
import dev.thilanka.netrics.service.LteFddBasicKpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefddbasickpi")
@RequiredArgsConstructor
public class LteFddBasicKpiController {
    private final LteFddBasicKpiService lteFddBasicKpiService;

    @GetMapping
    ResponseEntity<List<LteFddBasicKpiDto>> getAll(){
        List<LteFddBasicKpiDto> dtos = lteFddBasicKpiService.getAll();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    ResponseEntity<LteFddBasicKpiDto> createBasicKpi(@RequestBody @Valid LteFddBasicKpiDto dto){
        LteFddBasicKpiDto savedDto = lteFddBasicKpiService.createBasicKpi(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }
}
