package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.service.SectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pulse/sectors")
public class SectorController {
    private final SectorService sectorService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PostMapping
    public ResponseEntity<SectorDto> createSector(@RequestBody @Valid SectorDto sectorDto) {
        return ResponseEntity.ok(sectorService.createSector(sectorDto));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PostMapping("list")
    public ResponseEntity<List<SectorDto>> createSector(@RequestBody @Valid List<SectorDto> sectorDtos) {
        return ResponseEntity.ok(sectorService.createSectors(sectorDtos));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("{name}")
    public ResponseEntity<SectorDto> getSectorByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(sectorService.getBySectorName(name));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("site-index")
    public ResponseEntity<SectorDto> getSectorByNameAndIndex(@RequestParam("siteCode") String siteCode, @RequestParam("sectorIndex") Integer sectorIndex) {
        return ResponseEntity.ok(sectorService.getBySiteAndIndex(siteCode, sectorIndex));
    }
}
