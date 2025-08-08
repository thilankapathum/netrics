package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddKpiMappingDto;
import dev.thilanka.netrics.repository.LteFddKpiMappingRepository;
import dev.thilanka.netrics.service.LteFddKpiMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefddkpimapping")
@RequiredArgsConstructor
public class LteFddKpiMappingController {
    private final LteFddKpiMappingService lteFddKpiMappingService;

    @GetMapping
    public ResponseEntity<List<LteFddKpiMappingDto>> getAll(){
        List<LteFddKpiMappingDto> dto = lteFddKpiMappingService.getAll();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<LteFddKpiMappingDto> createMapping(@RequestBody @Valid LteFddKpiMappingDto dto){
        LteFddKpiMappingDto savedDto = lteFddKpiMappingService.createLteFddKpiMapping(dto);
        return new ResponseEntity<>(savedDto,HttpStatus.CREATED);
    }
}
