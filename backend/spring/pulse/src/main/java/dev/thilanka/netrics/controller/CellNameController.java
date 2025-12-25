package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.service.CellNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/pulse/cell-name")
@RequiredArgsConstructor
public class CellNameController {
    private final CellNameService cellNameService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<List<CellNameDto>> searchCell(@RequestParam("name") String cellName){
        List<CellNameDto> cells = cellNameService.searchCell(cellName);
        return ResponseEntity.ok(cells);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_PULSE_UPDATE','ROLE_PULSE_CREATE')")
    @PostMapping("reload")
    public ResponseEntity<String> reloadCells(){
        CompletableFuture<Integer> size = cellNameService.reloadCells();
        int cellSize = 0;
        try {
            cellSize = size.get();
        } catch (Exception e) {
            System.out.println("Error retrieving cell size");
        }
        return ResponseEntity.ok( cellSize + " Cells Reloaded");
    }
}
