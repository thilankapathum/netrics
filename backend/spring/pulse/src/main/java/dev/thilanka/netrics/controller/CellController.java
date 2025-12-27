package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.service.CellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/cells")
@RequiredArgsConstructor
public class CellController {
    private final CellService cellService;

    @PostMapping
    public ResponseEntity<CellDto> createCell(@RequestBody @Valid CellDto dto){
        CellDto savedDto = cellService.createCell(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    public ResponseEntity<List<CellDto>> createCells(@RequestBody @Valid List<CellDto> dtos){
        List<CellDto> savedDtos = cellService.createCells(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CellDto> getCellByName(@RequestParam("cellName") String cellName){
        CellDto dto = cellService.getByCellName(cellName);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<CellDto> updateCell(@RequestBody @Valid CellDto dto){
        CellDto updatedDto = cellService.updateCell(dto);
        return new ResponseEntity<>(updatedDto,HttpStatus.CREATED);
    }

    @PutMapping("list")
    public ResponseEntity<List<CellDto>> updateCells(@RequestBody @Valid List<CellDto> dtos){
        List<CellDto> updatedDtos = cellService.updateCells(dtos);
        return new ResponseEntity<>(updatedDtos,HttpStatus.CREATED);
    }
}
