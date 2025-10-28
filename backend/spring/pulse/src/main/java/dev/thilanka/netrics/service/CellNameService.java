package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellNameDto;

import java.util.List;

public interface CellNameService {

    List<CellNameDto> searchCell(String cellName);

    int reloadCells();
}
