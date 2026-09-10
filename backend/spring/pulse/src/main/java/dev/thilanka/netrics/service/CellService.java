package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellCsvImportResultDto;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.dto.CellUpdateResult;
import dev.thilanka.netrics.entity.Cell;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CellService {

    Cell createCell(Cell cell);

    CellDto createCell(CellDto dto);

    List<CellDto> createCells(List<CellDto> dtos);

    Cell updateCell(Cell cell);

    CellUpdateResult updateCell(CellDto dto);

    List<CellDto> updateCells(List<CellDto> dtos);

    List<CellCsvImportResultDto> updateCellsWithResult(List<CellDto> dtos);

    Cell findByCellName(String cellName);

    CellDto getByCellName(String cellName);

    List<Cell> findAllCells();

    List<CellDto> getAllCells();

    List<CellDto> getAllCellInfo();

    List<CellNameDto> searchCell(String cellName);

    List<CellNameDto> getCellsBySector(String sectorName, String ratName);

    List<CellDto> createLatestCells(String ratName, String granularityName);

    CompletableFuture<Integer> reloadCells();

    CompletableFuture<Integer> reloadCells(String ratName, String granularityName);

    List<Cell> findCellsWithMissingInfo();

    List<CellDto> getCellsWithMissingInfo();

    Integer findCellCountWithMissingInfo();

    Integer reloadCellCountWithMissingInfo();

    Integer getCellCountWithMissingInfo();

    int updateNodeNamesFromLatestKpiValues();
}
