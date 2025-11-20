package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.CellNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CellNameServiceImpl implements CellNameService {

    private final KpiDayRepository kpiDayRepository;

    List<CellNameDto> cellNames = new ArrayList<>();

    @Override
    @Async("cacheExecutor")
    public CompletableFuture<Integer> reloadCells() {
        System.out.println("reloading cells...");
        this.cellNames = kpiDayRepository.getAllCellNames();
        System.out.println("Loaded " + this.cellNames.size() + " cells");
        return CompletableFuture.completedFuture(this.cellNames.size());
    }

    @Override
    public List<CellNameDto> searchCell(String cellName) {

        if (this.cellNames.isEmpty()) {
            System.out.println("cellNames List is empty");
            reloadCells();
        }

        if (cellName == null || cellName.isBlank()) {
            return Collections.emptyList();
        } else {
            String lower = cellName.toLowerCase();
            return cellNames.stream()
                    .filter(c -> c.cellName().toLowerCase().contains(lower))
                    .limit(20)
                    .toList();
        }
    }

}
