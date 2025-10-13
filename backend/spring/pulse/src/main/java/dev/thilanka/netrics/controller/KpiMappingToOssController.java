package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.KpiMappingToOssDto;
import dev.thilanka.netrics.service.KpiMappingToOssService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/kpimappingtooss")
@RequiredArgsConstructor
public class KpiMappingToOssController {
    private final KpiMappingToOssService kpiMappingToOssService;

    @GetMapping
    public ResponseEntity<List<KpiMappingToOssDto>> getAll(@RequestParam("ratName") String ratName){
        List<KpiMappingToOssDto> dto = kpiMappingToOssService.getAll(ratName);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<KpiMappingToOssDto> createMapping(@RequestBody @Valid KpiMappingToOssDto dto){
        KpiMappingToOssDto savedDto = kpiMappingToOssService.createLteFddKpiMapping(dto);
        return new ResponseEntity<>(savedDto,HttpStatus.CREATED);
    }

    @PostMapping("list")
    public ResponseEntity<List<KpiMappingToOssDto>> createMapping(@RequestBody @Valid List<KpiMappingToOssDto> dtos){
        List<KpiMappingToOssDto> savedDtoList = kpiMappingToOssService.createLteFddKpiMappingList(dtos);
        return new ResponseEntity<>(savedDtoList,HttpStatus.CREATED);
    }
}
