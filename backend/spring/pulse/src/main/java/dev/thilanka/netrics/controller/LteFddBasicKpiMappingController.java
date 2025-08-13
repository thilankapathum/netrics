package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.LteFddBasicKpiMappingDto;
import dev.thilanka.netrics.service.LteFddBasicKpiMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefddbasickpimapping")
@RequiredArgsConstructor
public class LteFddBasicKpiMappingController {
//    private final LteFddBasicKpiMappingService lteFddBasicKpiMappingService;

//    @GetMapping
//    ResponseEntity<List<LteFddBasicKpiMappingDto>> getAll(){
//        List<LteFddBasicKpiMappingDto> dtos = lteFddBasicKpiMappingService.getAll();
//        return ResponseEntity.ok(dtos);
//    }
//
//    @PostMapping
//    ResponseEntity<LteFddBasicKpiMappingDto> createBasicKpiMapping(@RequestBody @Valid LteFddBasicKpiMappingDto dto){
//        LteFddBasicKpiMappingDto savedDto = lteFddBasicKpiMappingService.createMapping(dto);
//        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
//    }
}
