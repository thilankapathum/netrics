package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AreaTypeDto;
import dev.thilanka.netrics.service.AreaTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/area-types")
@RequiredArgsConstructor
public class AreaTypeController {
    private final AreaTypeService areaTypeService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    ResponseEntity<AreaTypeDto> createAreaType(@RequestBody @Valid AreaTypeDto dto){
        AreaTypeDto savedAreaType = areaTypeService.createAreaType(dto);
        return new ResponseEntity<>(savedAreaType, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    ResponseEntity<List<AreaTypeDto>> getAll(){
        List<AreaTypeDto> dtos = areaTypeService.getAll();
        return new ResponseEntity<>(dtos,HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("list")
    ResponseEntity<List<AreaTypeDto>> createAreaTypes(@RequestBody @Valid List<AreaTypeDto> dtos){
        List<AreaTypeDto> savedAreaType = areaTypeService.createAreaTypes(dtos);
        return new ResponseEntity<>(savedAreaType, HttpStatus.CREATED);
    }
}
