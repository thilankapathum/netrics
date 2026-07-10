package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.entity.Cell;
import dev.thilanka.netrics.entity.CellName;
import dev.thilanka.netrics.repository.CellNameRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.CellNameService;
import dev.thilanka.netrics.service.CellService;
import dev.thilanka.netrics.service.DateService;
import dev.thilanka.netrics.service.KpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CellNameServiceImpl implements CellNameService {

    /* DEPRECATED */

    private final KpiDayRepository kpiDayRepository;
    private final KpiDayService kpiDayService;
    private final CellNameRepository cellNameRepository;
    private final DateService dateService;
    private final CellService cellService;

    List<CellNameDto> cellNames = new ArrayList<>();

    @Override
    @Async("cacheExecutor")
    public CompletableFuture<Integer> reloadCells() {
        System.out.println("reloading cells...");
        List<CellName> cellNameList = findAllCellNames();
        this.cellNames = cellNameList
                .stream()
                .map(cn -> new CellNameDto(cn.getCellName(), cn.getRatName(), cn.getRatLabel()))
                .collect(Collectors.toList());
        System.out.println("Loaded " + this.cellNames.size() + " cells");
        return CompletableFuture.completedFuture(this.cellNames.size());
    }

    @Override
    @Async("cacheExecutor")
    public CompletableFuture<Integer> reloadCells(String ratName, String granularityName) {

        System.out.println("[" + ratName + " - " + granularityName + "] Creating Cell Names...");
        List<CellNameDto> cellNameDtos = createLatestCellNameList(ratName, granularityName);
        System.out.println("[" + granularityName + " - " + ratName + "] created " + cellNameDtos.size() + " Cell Names");
        List<CellName> cellNameList = findAllCellNames();
        this.cellNames = cellNameList
                .stream()
                .map(cn -> new CellNameDto(cn.getCellName(), cn.getRatName(), cn.getRatLabel()))
                .collect(Collectors.toList());
        System.out.println("LOADED " + this.cellNames.size() + " CELLS TO MEMORY!");

        System.out.println("[" + ratName + " - " + granularityName + "] Creating Cells...");
        List<CellDto> cellDtos = cellService.createLatestCells(ratName, granularityName);
        System.out.println("[" + granularityName + " - " + ratName + "] created " + cellDtos.size() + " Cells");


        return CompletableFuture.completedFuture(this.cellNames.size());
    }

    @Override
    public CellName createCellName(CellName cellName) {
        return cellNameRepository.save(cellName);
    }

    @Override
    public CellNameDto createCellName(CellNameDto cellNameDto) {
        Optional<CellName> existingCellName = findCellNameByName(cellNameDto.cellName());
        if (existingCellName.isPresent()) {
            return null;
        } else {
            CellName cellName = CellName.builder()
                    .cellName(cellNameDto.cellName())
                    .ratName(cellNameDto.ratName())
                    .ratLabel(cellNameDto.ratLabel())
                    .build();
            CellName savedCellName = createCellName(cellName);
            return new CellNameDto(savedCellName.getCellName(), savedCellName.getRatName(), savedCellName.getRatLabel());
        }
    }

    @Override
    public List<CellNameDto> createCellNameList(List<CellNameDto> cellNameDtos) {
        List<CellNameDto> savedCellNameDtos = new ArrayList<>();
        int errorCounter = 0;

        for (CellNameDto dto : cellNameDtos) {
            CellNameDto savedDto = createCellName(dto);
            if (savedDto == null) {
                errorCounter++;
                continue;
            }
            savedCellNameDtos.add(savedDto);
        }

        if (errorCounter > 0) {
            System.out.println("Error saving " + errorCounter + "/" + (errorCounter + savedCellNameDtos.size()) + " cells");
        }

        return savedCellNameDtos;
    }

    @Override
    public List<CellNameDto> createLatestCellNameList(String ratName, String granularityName) {

        LocalDateTime timestamp = dateService.getLatestDate(ratName, granularityName)
                .toLocalDate().atStartOfDay().plusSeconds(86399);
        LocalDateTime preTimestamp = timestamp.toLocalDate().atStartOfDay();

        List<CellNameDto> cellNameDtos = kpiDayService.getCellNamesByTimestamps(timestamp, preTimestamp, ratName, granularityName);
        return createCellNameList(cellNameDtos);
    }

    @Override
    public List<CellName> findAllCellNames() {
        return cellNameRepository.findAll();
    }

    @Override
    public Optional<CellName> findCellNameByName(String cellName) {
        return cellNameRepository.findByCellName(cellName);
    }

    @Override
    public List<CellNameDto> getAllCellNames() {
        List<CellName> cellNameList = findAllCellNames();
        return cellNameList
                .stream()
                .map(cn -> new CellNameDto(cn.getCellName(), cn.getRatName(), cn.getRatLabel()))
                .collect(Collectors.toList());
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
