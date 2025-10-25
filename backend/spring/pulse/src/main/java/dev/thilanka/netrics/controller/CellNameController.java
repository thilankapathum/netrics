package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.service.CellNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/cell")
@RequiredArgsConstructor
public class CellNameController {
    private final CellNameService cellNameService;

    @GetMapping
    public ResponseEntity<List<CellNameDto>> searchCell(@RequestParam("name") String cellName){
        List<CellNameDto> cells = cellNameService.searchCell(cellName);
        return ResponseEntity.ok(cells);
    }

    @PostMapping("reload")
    public ResponseEntity<String> reloadCells(){
        int cellSize = cellNameService.reloadCells();
        return ResponseEntity.ok( cellSize + "Cells Reloaded");
    }
}
