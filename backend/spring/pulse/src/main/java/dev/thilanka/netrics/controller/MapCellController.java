package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.service.MapCellService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
                                                       @RequestParam("date") String date,
                                                       @RequestParam("areaName") String areaName) {
        List<MapCell> mapCells = mapCellService.getMapCellsByKpi(minLng, minLat, maxLng, maxLat, standardKpiName, ratName, granularityName, date, areaName);
        return ResponseEntity.ok(mapCells);
    }


    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("tile/{z}/{x}/{y}")
    public ResponseEntity<List<MapCell>> getCellsByTile(
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            @RequestParam String standardKpiName,
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam String date,
            @RequestParam String areaName) {

        List<MapCell> cells = mapCellService
                .getMapCellsByTile(z, x, y, standardKpiName, ratName,
                        granularityName, date, areaName);
        return ResponseEntity.ok(cells);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("tile-band/{z}/{x}/{y}")
    public ResponseEntity<List<MapCell>> getCellsByTileAndBand(
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            @RequestParam String standardKpiName,
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam String date,
            @RequestParam String areaName,
            @RequestParam String bandName) {

        List<MapCell> cells = mapCellService
                .getMapCellsByTileAndBand(z, x, y, standardKpiName, ratName,
                        granularityName, date, areaName, bandName);
        return ResponseEntity.ok(cells);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("site-tile/{z}/{x}/{y}")
    public ResponseEntity<List<SiteDto>> getSitesByTile(
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y
    ){
        List<SiteDto> sites = mapCellService.getSitesByTile(z, x, y);
        return ResponseEntity.ok(sites);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    @GetMapping(value = "tileX/{z}/{x}/{y}", produces = "application/x-protobuf")
    public ResponseEntity<byte[]> getTile(@PathVariable("z") int z,
                                          @PathVariable("x") int x,
                                          @PathVariable("y") int y,
                                          @RequestParam("standardKpiName") String standardKpiName,
                                          @RequestParam("ratName") String ratName,
                                          @RequestParam("granularityName") String granularityName,
                                          @RequestParam("date") String date,
                                          @RequestParam("areaName") String areaName) {
        byte[] tile = mapCellService.getTile(z, x, y, standardKpiName, ratName, granularityName, date, areaName);

        return ResponseEntity.ok()
                .header("Content-Type", "application/x-protobuf")
                .body(tile);
    }
}
