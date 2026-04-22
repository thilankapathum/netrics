package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.service.MapCellService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/map-cells")
@RequiredArgsConstructor
public class MapCellController {
    private final MapCellService mapCellService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("standard-kpi")
    public ResponseEntity<List<MapCell>> getCellsByKpi(@RequestParam("minLng") Double minLng,
                                                       @RequestParam("minLat") Double minLat,
                                                       @RequestParam("maxLng") Double maxLng,
                                                       @RequestParam("maxLat") Double maxLat,
                                                       @RequestParam("standardKpiName") String standardKpiName,
                                                       @RequestParam("ratName") String ratName,
                                                       @RequestParam("granularityName") String granularityName,
                                                       @RequestParam("date") String date) {
        List<MapCell> mapCells = mapCellService.getMapCellsByKpi(minLng, minLat, maxLng, maxLat, standardKpiName, ratName, granularityName, date);
        return ResponseEntity.ok(mapCells);
    }
}
