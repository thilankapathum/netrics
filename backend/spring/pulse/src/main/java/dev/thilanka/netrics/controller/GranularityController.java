package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.GranularityDto;
import dev.thilanka.netrics.service.GranularityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/granularity")
@RequiredArgsConstructor
public class GranularityController {
    private final GranularityService granularityService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    public ResponseEntity<GranularityDto> createGranularity(@RequestBody @Valid GranularityDto dto){
        GranularityDto savedGranularity = granularityService.createGranularity(dto);
        return new ResponseEntity<>(savedGranularity, HttpStatus.CREATED);
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    public ResponseEntity<List<GranularityDto>> createGranularityList(@RequestBody @Valid List<GranularityDto> dtos){
        List<GranularityDto> granularityDtos = granularityService.createGranularityList(dtos);
        return new ResponseEntity<>(granularityDtos,HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<GranularityDto>> getAll(){
        List<GranularityDto> granularityDtos = granularityService.getAll();
        return ResponseEntity.ok(granularityDtos);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<GranularityDto> getGranularityById(@PathVariable("id") Long id){
        GranularityDto granularityDto = granularityService.getGranularityById(id);
        return ResponseEntity.ok(granularityDto);
    }
}
