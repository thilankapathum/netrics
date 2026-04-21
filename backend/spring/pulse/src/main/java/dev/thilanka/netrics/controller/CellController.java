package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellCsvImportResultDto;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.CellUpdateResult;
import dev.thilanka.netrics.service.CellService;
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
@RequestMapping("/api/v1/pulse/cells")
@RequiredArgsConstructor
public class CellController {
    private final CellService cellService;
    private final CsvService csvService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    public ResponseEntity<CellDto> createCell(@RequestBody @Valid CellDto dto) {
        CellDto savedDto = cellService.createCell(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<CellDto>> createCells(@RequestBody @Valid List<CellDto> dtos) {
        List<CellDto> savedDtos = cellService.createCells(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<CellDto> getCellByName(@RequestParam("cellName") String cellName) {

        System.out.println("RAW cellName = [" + cellName + "]");
        System.out.println("LENGTH = " + cellName.length());

        CellDto dto = cellService.getByCellName(cellName);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("all/export")
    public void exportAllCells(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_cells_info.csv");

        List<CellDto> cellDtos = cellService.getAllCellInfo();

        csvService.writeCellsToCsv(cellDtos, response.getWriter());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PutMapping
    public ResponseEntity<CellDto> updateCell(@RequestBody @Valid CellDto dto) {
        CellUpdateResult updatedDto = cellService.updateCell(dto);
        return new ResponseEntity<>(updatedDto.cellDto(), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PutMapping("list")
    public ResponseEntity<List<CellDto>> updateCells(@RequestBody @Valid List<CellDto> dtos) {
        List<CellDto> updatedDtos = cellService.updateCells(dtos);
        return new ResponseEntity<>(updatedDtos, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("missing/export")
    public void exportCellsWithMissingInfo(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cells_missing_info.csv");

        List<CellDto> cellDtos = cellService.getCellsWithMissingInfo();

        csvService.writeCellsToCsv(cellDtos, response.getWriter());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping(value = "missing/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity importCellsWithCorrectedInfo(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        List<CellDto> importedCellDtos = csvService.readCellsFromCsv(file.getInputStream());
        List<CellCsvImportResultDto> results = cellService.updateCellsWithResult(importedCellDtos);
        cellService.reloadCellCountWithMissingInfo();

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = cell_import_result.csv");
        csvService.writeCellImportResultToCsv(results, response.getWriter());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("missing/count")
    public ResponseEntity<Integer> getCellCountWithMissingInfo(){
        Integer count = cellService.getCellCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("missing/reload-count")
    public ResponseEntity<Integer> reloadCellCountWithMissingInfo(){
        Integer count = cellService.reloadCellCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }
}
