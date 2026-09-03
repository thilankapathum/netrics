package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellMappingCsvImportResultDto;
import dev.thilanka.netrics.dto.CellMappingDto;
import dev.thilanka.netrics.service.CellMappingService;
import dev.thilanka.netrics.service.CsvService;
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
@RequestMapping("/api/v1/pulse/cell-mappings")
@RequiredArgsConstructor
public class CellMappingController {
    private final CellMappingService cellMappingService;
    private final CsvService csvService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    public ResponseEntity<CellMappingDto> createCellMapping(@RequestBody @Valid CellMappingDto dto) {
        CellMappingDto saved = cellMappingService.createCellMapping(dto.previousCellName(), dto.newCellName());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> importCellMappings(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        List<CellMappingDto> importedDtos = csvService.readCellMappingsFromCsv(file.getInputStream());
        List<CellMappingCsvImportResultDto> results = cellMappingService.createCellMappingsFromCsv(importedDtos);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = cell_mapping_import_result.csv");
        csvService.writeCellMappingImportResultToCsv(results, response.getWriter());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<List<CellMappingDto>> getAll() {
        return ResponseEntity.ok(cellMappingService.getAll());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCellMapping(@PathVariable Long id) {
        cellMappingService.deleteCellMapping(id);
        return ResponseEntity.noContent().build();
    }
}
