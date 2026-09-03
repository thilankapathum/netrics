package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellMappingCsvImportResultDto;
import dev.thilanka.netrics.dto.CellMappingDto;

import java.util.List;

public interface CellMappingService {

    CellMappingDto createCellMapping(String previousCellName, String newCellName);

    List<CellMappingCsvImportResultDto> createCellMappingsFromCsv(List<CellMappingDto> dtos);

    void deleteCellMapping(Long id);

    List<CellMappingDto> getAll();
}
