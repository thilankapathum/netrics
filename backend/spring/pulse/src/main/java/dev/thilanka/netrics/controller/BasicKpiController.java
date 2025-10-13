package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.service.BasicKpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/ltefdd/basickpi")
@RequiredArgsConstructor
public class BasicKpiController {
    private final BasicKpiService basicKpiService;

    @GetMapping
    ResponseEntity<List<BasicKpiDto>> getAllByRat(@RequestParam String ratName){
        List<BasicKpiDto> dtos = basicKpiService.getAllByRat(ratName);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    ResponseEntity<BasicKpiDto> createBasicKpi(@RequestBody @Valid BasicKpiDto dto){
        BasicKpiDto savedDto = basicKpiService.createBasicKpi(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }
}
