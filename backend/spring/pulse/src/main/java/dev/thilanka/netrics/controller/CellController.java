package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellCsvImportResultDto;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.service.CellService;
import dev.thilanka.netrics.service.CsvService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<CellDto> createCell(@RequestBody @Valid CellDto dto) {
        CellDto savedDto = cellService.createCell(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    public ResponseEntity<List<CellDto>> createCells(@RequestBody @Valid List<CellDto> dtos) {
        List<CellDto> savedDtos = cellService.createCells(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CellDto> getCellByName(@RequestParam("cellName") String cellName) {
        CellDto dto = cellService.getByCellName(cellName);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<CellDto> updateCell(@RequestBody @Valid CellDto dto) {
        CellDto updatedDto = cellService.updateCell(dto);
        return new ResponseEntity<>(updatedDto, HttpStatus.CREATED);
    }

    @PutMapping("list")
    public ResponseEntity<List<CellDto>> updateCells(@RequestBody @Valid List<CellDto> dtos) {
        List<CellDto> updatedDtos = cellService.updateCells(dtos);
        return new ResponseEntity<>(updatedDtos, HttpStatus.CREATED);
    }

    @GetMapping("missing/export")
    public void exportCellsWithMissingInfo(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cells_missing_info.csv");

        List<CellDto> cellDtos = cellService.getCellsWithMissingInfo();

        csvService.writeCellsToCsv(cellDtos, response.getWriter());
    }

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

    @GetMapping("missing/count")
    public ResponseEntity<Integer> getCellCountWithMissingInfo(){
        Integer count = cellService.getCellCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }

    @GetMapping("missing/reload-count")
    public ResponseEntity<Integer> reloadCellCountWithMissingInfo(){
        Integer count = cellService.reloadCellCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }
}
