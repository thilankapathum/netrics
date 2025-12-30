package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.CellCsvImportResultDto;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.enums.CsvImportStatus;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CellRepository;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CellServiceImpl implements CellService {
    private final CellRepository cellRepository;
    private final RatService ratService;
    private final SiteService siteService;
    private final BandService bandService;
    private final Mapper mapper;
    private final GranularityService granularityService;
    private final KpiDayService kpiDayService;
    private final DateService dateService;

    Integer cellCountWithMissingInfo = 0;

    @Override
    public Cell createCell(Cell cell) {
        return cellRepository.save(cell);
    }

    @Override
    public CellDto createCell(CellDto dto) {
        Cell savedCell = createCell(dtoToCell(dto));
        return mapper.cellToDto(savedCell);
    }

    private Cell dtoToCell(CellDto dto) {
        Cell.CellBuilder cell = Cell.builder().cellName(dto.cellName());

        if (dto.nodeName() != null) {
            cell.nodeName(dto.nodeName());
        }

        if (dto.ratName() != null) {
            Rat rat = ratService.findRatByName(dto.ratName());
            cell.rat(rat);
        }

        if (dto.siteCode() != null) {
            Site site = siteService.findBySiteCode(dto.siteCode());
            cell.site(site);
        }

        if (dto.bandName() != null) {
            Band band = bandService.findByName(dto.bandName());
            cell.band(band);
        }

        return cell.build();
    }

    @Override
    public List<CellDto> createCells(List<CellDto> dtos) {

        List<CellDto> savedDtos = new ArrayList<>();
        int duplicateCells = 0;
        int failedCells = 0;

        for (CellDto dto : dtos) {
            Optional<Cell> cell = cellRepository.findByCellName(dto.cellName());
            if (cell.isPresent()) {
                duplicateCells++;
            } else {

                try {
                    CellDto savedDto = createCell(dto);
                    savedDtos.add(savedDto);
                } catch (Exception e) {
                    failedCells++;
                    System.out.println("failed saving cell. " + e.getMessage());
                }
            }
        }

        System.out.print("Saved " + savedDtos.size() + "/" + dtos.size() + " cells. ");
        if (duplicateCells > 0) System.out.println("Duplicate cells " + duplicateCells + "/" + dtos.size() + " found.");
        if (failedCells > 0) System.out.println("Failed saving " + failedCells + "/" + dtos.size() + " cells.");
        System.out.println(" ");
        return savedDtos;
    }

    @Override
    public Cell updateCell(Cell cell) {

        Optional<Cell> existingCell = cellRepository.findByCellName(cell.getCellName());

        existingCell.ifPresent(value -> cell.setId(value.getId()));
        return cellRepository.save(cell);
    }

    @Override
    public CellDto updateCell(CellDto dto) {
        Cell cell = dtoToCell(dto);
        return mapper.cellToDto(updateCell(cell));
    }

    @Override
    public List<CellDto> updateCells(List<CellDto> dtos) {
        List<CellDto> updatedCells = new ArrayList<>();

        for (CellDto dto : dtos) {
            try {
                CellDto updatedCell = updateCell(dto);
                updatedCells.add(updatedCell);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error updating cell due to: " + e.getMessage());
            }
        }

        return updatedCells;
    }

    @Override
    public List<CellCsvImportResultDto> updateCellsWithResult(List<CellDto> dtos) {
        List<CellCsvImportResultDto> importResultDtos = new ArrayList<>();

        for (CellDto dto : dtos) {
            try {
                CellDto updatedCell = updateCell(dto);
                importResultDtos.add(new CellCsvImportResultDto(updatedCell, CsvImportStatus.SUCCESS, ""));
            } catch (Exception e) {
                System.out.println(dto.cellName() + " Error: " + e.getMessage());
                importResultDtos.add(new CellCsvImportResultDto(dto, CsvImportStatus.FAIL, e.getMessage()));
            }
        }
        return importResultDtos;
    }

    @Override
    public Cell findByCellName(String cellName) {
        return cellRepository.findByCellName(cellName)
                .orElseThrow(() -> new RuntimeException("Cell not found by " + cellName));
    }

    @Override
    public CellDto getByCellName(String cellName) {
        Cell cell = findByCellName(cellName);
        return mapper.cellToDto(cell);
    }

    @Override
    public List<CellDto> createLatestCells(String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime timestamp = dateService.getLatestDate(ratName, granularityName)
                .toLocalDate().atStartOfDay().plusSeconds(86399);
        LocalDateTime preTimestamp = timestamp.toLocalDate().atStartOfDay();

        List<CellDto> dtos = kpiDayService.getCellsByTimestamp(timestamp, preTimestamp, rat, granularity);

        return createCells(dtos);
    }

    @Override
    public CompletableFuture<Integer> reloadCells(String ratName, String granularityName) {
        return null;    //TODO
    }

    @Override
    public List<Cell> findCellsWithMissingInfo() {
        return cellRepository.findCellsWithMissingInfo();
    }

    @Override
    public List<CellDto> getCellsWithMissingInfo() {
        List<Cell> cells = findCellsWithMissingInfo();
        return cells.stream().map(mapper::cellToDto).collect(Collectors.toList());
    }

    @Override
    public Integer findCellCountWithMissingInfo() {
        this.cellCountWithMissingInfo = cellRepository.findCellCountWithMissingInfo();
        return this.cellCountWithMissingInfo;
    }

    @Override
    public Integer getCellCountWithMissingInfo() {
        return this.cellCountWithMissingInfo;
    }
}
