package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.entity.Cell;

import java.util.List;

public interface CellService {

    Cell createCell(Cell cell);

    CellDto createCell(CellDto dto);

    List<CellDto> createCells(List<CellDto> dtos);

    Cell findByCellName(String cellName);

    CellDto getByCellName(String cellName);
}
