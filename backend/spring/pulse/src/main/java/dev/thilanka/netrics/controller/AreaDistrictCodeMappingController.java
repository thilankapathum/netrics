package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AreaDistrictCodeMappingDto;
import dev.thilanka.netrics.service.AreaDistrictCodeMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/area-district-code")
@RequiredArgsConstructor
public class AreaDistrictCodeMappingController {
    private final AreaDistrictCodeMappingService areaDistrictCodeMappingService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    ResponseEntity<AreaDistrictCodeMappingDto> createAreaDistrictCodeMapping(@RequestBody @Valid AreaDistrictCodeMappingDto dto){
        AreaDistrictCodeMappingDto saved = areaDistrictCodeMappingService.createAreaDistrictCodeMapping(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("list")
    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    ResponseEntity<List<AreaDistrictCodeMappingDto>> createAreaDistrictCodeMappings(@RequestBody @Valid List<AreaDistrictCodeMappingDto> dtos){
        List<AreaDistrictCodeMappingDto> saved = areaDistrictCodeMappingService.createAreaDistrictCodeMappings(dtos);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    ResponseEntity<List<AreaDistrictCodeMappingDto>> getAll(){
        return ResponseEntity.ok(areaDistrictCodeMappingService.getAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    ResponseEntity<Void> deleteAreaDistrictCodeMapping(@PathVariable("id") Long id){
        areaDistrictCodeMappingService.deleteAreaDistrictCodeMapping(id);
        return ResponseEntity.noContent().build();
    }
}
