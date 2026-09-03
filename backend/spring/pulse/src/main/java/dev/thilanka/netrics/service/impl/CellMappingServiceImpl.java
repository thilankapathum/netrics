package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.DuplicateResourceException;
import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.CellMappingDto;
import dev.thilanka.netrics.entity.Cell;
import dev.thilanka.netrics.entity.CellMapping;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CellMappingRepository;
import dev.thilanka.netrics.service.CellMappingService;
import dev.thilanka.netrics.service.CellService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CellMappingServiceImpl implements CellMappingService {

    private final CellMappingRepository cellMappingRepository;
    private final CellService cellService;
    private final Mapper mapper;

    @Override
    public CellMappingDto createCellMapping(String previousCellName, String newCellName) {
        if (previousCellName.equalsIgnoreCase(newCellName)) {
            throw new DuplicateResourceException("CellMapping", "cellName", previousCellName);
        }

        Cell previousCell = cellService.findByCellName(previousCellName);
        Cell newCell = cellService.findByCellName(newCellName);

        if (cellMappingRepository.existsByPreviousCell_Id(previousCell.getId())) {
            throw new DuplicateResourceException("CellMapping", "previousCellName", previousCellName);
        }
        if (cellMappingRepository.existsByNewCell_Id(newCell.getId())) {
            throw new DuplicateResourceException("CellMapping", "newCellName", newCellName);
        }

        CellDto previousDto = mapper.cellToDto(previousCell);
        CellDto newDto = mapper.cellToDto(newCell);

        // One-time inheritance: copy azimuth/beamwidth/isMultiBeam/site/band/carrier/sector from the
        // previous cell onto the new cell. nodeName/ratName/districtCode are left as the new cell's own.
        CellDto mergedDto = new CellDto(
                newDto.cellName(),
                newDto.nodeName(),
                newDto.ratName(),
                previousDto.siteCode(),
                previousDto.bandName(),
                previousDto.azimuth(),
                previousDto.beamwidth(),
                previousDto.isMultiBeam(),
                previousDto.carrierName(),
                previousDto.sectorName()
        );
        cellService.updateCell(mergedDto);

        CellMapping mapping = CellMapping.builder()
                .previousCell(previousCell)
                .newCell(newCell)
                .build();
        CellMapping saved = cellMappingRepository.save(mapping);

        return mapper.cellMappingToDto(saved);
    }

    @Override
    public void deleteCellMapping(Long id) {
        if (!cellMappingRepository.existsById(id)) {
            throw new ResourceNotFoundException("CellMapping", "id", id);
        }
        cellMappingRepository.deleteById(id);
    }

    @Override
    public List<CellMappingDto> getAll() {
        return cellMappingRepository.findAll()
                .stream()
                .map(mapper::cellMappingToDto)
                .toList();
    }
}
