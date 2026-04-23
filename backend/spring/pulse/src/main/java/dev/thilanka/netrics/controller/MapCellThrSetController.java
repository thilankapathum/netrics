package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.service.MapCellThrSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/map-cell-thr-sets")
@RequiredArgsConstructor
public class MapCellThrSetController {
    private final MapCellThrSetService mapCellThrSetService;
}
