package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.entity.CellName;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CellNameService {

    List<CellName> findAllCellNames();

    Optional<CellName> findCellNameByName(String cellName);

    List<CellNameDto> getAllCellNames();

    List<CellNameDto> searchCell(String cellName);

    CompletableFuture<Integer> reloadCells();

    CompletableFuture<Integer> reloadCells(String ratName, String granularityName);

    CellName createCellName(CellName cellName);

    CellNameDto createCellName(CellNameDto cellNameDto);

    List<CellNameDto> createCellNameList(List<CellNameDto> cellNameDtos);

    List<CellNameDto> createLatestCellNameList(String ratName, String granularityName);
}
