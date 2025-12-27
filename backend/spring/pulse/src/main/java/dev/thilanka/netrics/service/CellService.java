package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.entity.Cell;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CellService {

    Cell createCell(Cell cell);

    CellDto createCell(CellDto dto);

    List<CellDto> createCells(List<CellDto> dtos);

    Cell updateCell(Cell cell);

    CellDto updateCell(CellDto dto);

    List<CellDto> updateCells(List<CellDto> dtos);

    Cell findByCellName(String cellName);

    CellDto getByCellName(String cellName);

    List<CellDto> createLatestCells(String ratName, String granularityName);

    CompletableFuture<Integer> reloadCells(String ratName, String granularityName);

    List<Cell> findCellsWithMissingInfo();

    List<CellDto> getCellsWithMissingInfo();
}
