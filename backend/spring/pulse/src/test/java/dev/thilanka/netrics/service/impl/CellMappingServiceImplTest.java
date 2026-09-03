package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.CellMappingCsvImportResultDto;
import dev.thilanka.netrics.dto.CellMappingDto;
import dev.thilanka.netrics.entity.Cell;
import dev.thilanka.netrics.entity.CellMapping;
import dev.thilanka.netrics.entity.enums.CsvImportStatus;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CellMappingRepository;
import dev.thilanka.netrics.service.CellService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CellMappingServiceImplTest {

    @Mock
    private CellMappingRepository cellMappingRepository;
    @Mock
    private CellService cellService;
    @Mock
    private Mapper mapper;

    @InjectMocks
    private CellMappingServiceImpl cellMappingService;

    private Cell cellWithId(long id, String name) {
        Cell cell = mock(Cell.class);
        lenient().when(cell.getId()).thenReturn(id);
        lenient().when(cell.getCellName()).thenReturn(name);
        return cell;
    }

    @Test
    void createCellMappingsFromCsv_continuesPastFailingRowsAndReportsPerRowStatus() {
        Cell previousCellA = cellWithId(1L, "CELL_A");
        Cell newCellB = cellWithId(2L, "CELL_B");
        Cell previousCellC = cellWithId(3L, "CELL_C");
        Cell newCellD = cellWithId(4L, "CELL_D");

        when(cellService.findByCellName("CELL_A")).thenReturn(previousCellA);
        when(cellService.findByCellName("CELL_B")).thenReturn(newCellB);
        when(cellService.findByCellName("CELL_C")).thenReturn(previousCellC);
        when(cellService.findByCellName("CELL_D")).thenReturn(newCellD);
        when(cellService.findByCellName("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Cell", "cellName", "UNKNOWN"));

        when(cellMappingRepository.existsByPreviousCell_Id(anyLong())).thenReturn(false);
        when(cellMappingRepository.existsByNewCell_Id(anyLong())).thenReturn(false);

        when(mapper.cellToDto(any(Cell.class))).thenReturn(mock(CellDto.class));
        when(cellMappingRepository.save(any(CellMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.cellMappingToDto(any(CellMapping.class)))
                .thenAnswer(invocation -> {
                    CellMapping saved = invocation.getArgument(0);
                    return new CellMappingDto(99L, saved.getPreviousCell().getCellName(), saved.getNewCell().getCellName());
                });

        List<CellMappingDto> rows = List.of(
                new CellMappingDto(null, "CELL_A", "CELL_B"),
                new CellMappingDto(null, "CELL_C", "UNKNOWN"),
                new CellMappingDto(null, "CELL_C", "CELL_D")
        );

        List<CellMappingCsvImportResultDto> results = cellMappingService.createCellMappingsFromCsv(rows);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).status()).isEqualTo(CsvImportStatus.SUCCESS);
        assertThat(results.get(1).status()).isEqualTo(CsvImportStatus.FAIL);
        assertThat(results.get(1).errorMessage()).isNotBlank();
        assertThat(results.get(2).status()).isEqualTo(CsvImportStatus.SUCCESS);
    }
}
