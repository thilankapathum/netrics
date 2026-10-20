package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.CellAzimuthUpdateDto;
import dev.thilanka.netrics.entity.Cell;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CellRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CellServiceImplTest {

    @Mock
    private CellRepository cellRepository;
    @Mock
    private Mapper mapper;

    @InjectMocks
    private CellServiceImpl cellService;

    @Test
    void updateAzimuths_setsAzimuthOnEveryNamedCell() {
        Cell a = Cell.builder().cellName("KYAMP8-M-L18-A1").azimuth(80).build();
        Cell b = Cell.builder().cellName("KYAMP8-M-L21-A1").azimuth(80).build();
        when(cellRepository.findByCellName("KYAMP8-M-L18-A1")).thenReturn(Optional.of(a));
        when(cellRepository.findByCellName("KYAMP8-M-L21-A1")).thenReturn(Optional.of(b));
        when(cellRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        cellService.updateAzimuths(new CellAzimuthUpdateDto(List.of("KYAMP8-M-L18-A1", "KYAMP8-M-L21-A1"), 30));

        assertThat(a.getAzimuth()).isEqualTo(30);
        assertThat(b.getAzimuth()).isEqualTo(30);
        verify(cellRepository).saveAll(List.of(a, b));
        verify(mapper, times(2)).cellToDto(any());
    }

    @Test
    void updateAzimuths_unknownCellSavesNothing() {
        when(cellRepository.findByCellName("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cellService.updateAzimuths(new CellAzimuthUpdateDto(List.of("MISSING"), 30)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(cellRepository, never()).saveAll(anyList());
    }
}
