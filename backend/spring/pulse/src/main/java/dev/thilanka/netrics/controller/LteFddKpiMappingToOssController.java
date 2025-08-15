package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddKpiMappingToOssDto;
import dev.thilanka.netrics.service.LteFddKpiMappingToOssService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/kpimappingtooss")
@RequiredArgsConstructor
public class LteFddKpiMappingToOssController {
    private final LteFddKpiMappingToOssService lteFddKpiMappingToOssService;

    @GetMapping
    public ResponseEntity<List<LteFddKpiMappingToOssDto>> getAll(){
        List<LteFddKpiMappingToOssDto> dto = lteFddKpiMappingToOssService.getAll();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<LteFddKpiMappingToOssDto> createMapping(@RequestBody @Valid LteFddKpiMappingToOssDto dto){
        LteFddKpiMappingToOssDto savedDto = lteFddKpiMappingToOssService.createLteFddKpiMapping(dto);
        return new ResponseEntity<>(savedDto,HttpStatus.CREATED);
    }

    @PostMapping("list")
    public ResponseEntity<List<LteFddKpiMappingToOssDto>> createMapping(@RequestBody @Valid List<LteFddKpiMappingToOssDto> dtos){
        List<LteFddKpiMappingToOssDto> savedDtoList = lteFddKpiMappingToOssService.createLteFddKpiMappingList(dtos);
        return new ResponseEntity<>(savedDtoList,HttpStatus.CREATED);
    }
}
