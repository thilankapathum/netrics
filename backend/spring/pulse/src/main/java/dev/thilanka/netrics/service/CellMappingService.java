package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellMappingDto;

import java.util.List;

public interface CellMappingService {

    CellMappingDto createCellMapping(String previousCellName, String newCellName);

    void deleteCellMapping(Long id);

    List<CellMappingDto> getAll();
}
