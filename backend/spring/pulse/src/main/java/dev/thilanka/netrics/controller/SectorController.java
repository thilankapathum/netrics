package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.SectorCsvImportResultDto;
import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.service.CsvService;
import dev.thilanka.netrics.service.SectorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pulse/sectors")
public class SectorController {
    private final SectorService sectorService;
    private final CsvService csvService;

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

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("missing/export")
    public void exportCellsWithMissingInfo(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sectors_missing_info.csv");

        List<SectorDto> sectorDtos = sectorService.getSectorsWithMissingInfo();
        csvService.writeSectorsToCsv(sectorDtos, response.getWriter());
        sectorService.reloadSectorCountWithMissingInfo();
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("all/export")
    public void exportAllSectors(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_sectors_info.csv");

        List<SectorDto> sectorDtos = sectorService.getAllSectors();
        csvService.writeSectorsToCsv(sectorDtos, response.getWriter());
        sectorService.reloadSectorCountWithMissingInfo();
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping(value = "missing/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity importSitesWithCorrectedInfo(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        List<SectorDto> importedSectorDtos = csvService.readSectorsFromCsv(file.getInputStream());
        List<SectorCsvImportResultDto> results = sectorService.updateSectorsWithResults(importedSectorDtos);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = sector_import_result.csv");
        csvService.writeSectorImportResultToCsv(results, response.getWriter());
        sectorService.reloadSectorCountWithMissingInfo();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("missing/count")
    public ResponseEntity<Integer> getSectorCountWithMissingInfo(){
        Integer count = sectorService.getSectorCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("missing/reload-count")
    public ResponseEntity<Integer> reloadSectorCountWithMissingInfo(){
        Integer count = sectorService.reloadSectorCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }
}
