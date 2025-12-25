package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.entity.Band;
import dev.thilanka.netrics.entity.Cell;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.Site;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CellRepository;
import dev.thilanka.netrics.service.BandService;
import dev.thilanka.netrics.service.CellService;
import dev.thilanka.netrics.service.RatService;
import dev.thilanka.netrics.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CellServiceImpl implements CellService {
    private final CellRepository cellRepository;
    private final RatService ratService;
    private final SiteService siteService;
    private final BandService bandService;
    private final Mapper mapper;

    @Override
    public Cell createCell(Cell cell) {
        return cellRepository.save(cell);
    }

    @Override
    public CellDto createCell(CellDto dto) {

        Rat rat = ratService.findRatByName(dto.ratName());
        Site site = siteService.findBySiteCode(dto.siteCode());
        Band band = bandService.findByName(dto.bandName());

        Cell cell = Cell.builder()
                .cellName(dto.cellName())
                .nodeName(dto.nodeName())
                .rat(rat)
                .site(site)
                .band(band)
                .build();

        Cell savedCell = createCell(cell);
        return mapper.cellToDto(savedCell);
    }

    @Override
    public List<CellDto> createCells(List<CellDto> dtos) {

        List<CellDto> savedDtos = new ArrayList<>();

        for (CellDto dto: dtos){
            CellDto savedDto = createCell(dto);
            savedDtos.add(savedDto);
        }

        return savedDtos;
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
}
