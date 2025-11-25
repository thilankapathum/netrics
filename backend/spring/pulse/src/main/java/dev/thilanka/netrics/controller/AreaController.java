package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping
    ResponseEntity<AreaDto> createArea(@RequestBody @Valid AreaDto dto){
        AreaDto savedArea = areaService.createArea(dto);
        return new ResponseEntity<>(savedArea, HttpStatus.CREATED);
    }

    @GetMapping
    ResponseEntity<AreaDto> getAreaByName(@RequestParam("name") String name){
        AreaDto areaDto = areaService.getAreaByName(name);
        return ResponseEntity.ok(areaDto);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping("list")
    ResponseEntity<List<AreaDto>> createAreas(@RequestBody @Valid List<AreaDto> dtos){
        List<AreaDto> savedAreas = areaService.createAreas(dtos);
        return new ResponseEntity<>(savedAreas, HttpStatus.CREATED);
    }
}
