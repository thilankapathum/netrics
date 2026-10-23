package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.PulseClient;
import dev.thilanka.beam.common.exception.DependencyConflictException;
import dev.thilanka.beam.common.exception.ExternalServiceException;
import dev.thilanka.beam.dto.SectorEvent;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.Sector;
import dev.thilanka.beam.entity.Site;
import dev.thilanka.beam.repository.AntennaRepository;
import dev.thilanka.beam.repository.SectorRepository;
import dev.thilanka.beam.service.BeamEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectorServiceImplTest {

    @Mock
    private SectorRepository sectorRepository;
    @Mock
    private AntennaRepository antennaRepository;
    @Mock
    private PulseClient pulseClient;
    @Mock
    private BeamEventPublisher eventPublisher;

    @InjectMocks
    private SectorServiceImpl sectorService;

    private final Sector sector = Sector.builder()
            .id(9L).name("KYAMP8__9").sectorIndex(9)
            .site(Site.builder().siteCode("KYAMP8").build())
            .build();

    @BeforeEach
    void setUp() {
        when(sectorRepository.findByName("KYAMP8__9")).thenReturn(Optional.of(sector));
    }

    @Test
    void deleteSector_blockedByAntennas() {
        when(antennaRepository.findBySectorId(9L)).thenReturn(List.of(Antenna.builder().antennaIndex((byte) 1).build()));
        when(pulseClient.sectorCellNames("KYAMP8__9")).thenReturn(List.of());

        assertThatThrownBy(() -> sectorService.deleteSector("KYAMP8__9"))
                .isInstanceOf(DependencyConflictException.class);
        verify(sectorRepository, never()).delete(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deleteSector_blockedByPulseCells() {
        when(antennaRepository.findBySectorId(9L)).thenReturn(List.of());
        when(pulseClient.sectorCellNames("KYAMP8__9")).thenReturn(List.of("KYAMP8-M-L18-X1"));

        assertThat(sectorService.getDependents("KYAMP8__9")).containsExactly("Cell KYAMP8-M-L18-X1");
        assertThatThrownBy(() -> sectorService.deleteSector("KYAMP8__9"))
                .isInstanceOf(DependencyConflictException.class);
        verify(sectorRepository, never()).delete(any());
    }

    @Test
    void deleteSector_unusedSectorIsDeletedAndSynced() {
        when(antennaRepository.findBySectorId(9L)).thenReturn(List.of());
        when(pulseClient.sectorCellNames("KYAMP8__9")).thenReturn(List.of());

        sectorService.deleteSector("KYAMP8__9");

        verify(sectorRepository).delete(sector);
        ArgumentCaptor<SectorEvent> event = ArgumentCaptor.forClass(SectorEvent.class);
        verify(eventPublisher).publishSectorEvent(event.capture());
        assertThat(event.getValue().eventType()).isEqualTo("DELETED");
        assertThat(event.getValue().name()).isEqualTo("KYAMP8__9");
    }

    @Test
    void deleteSector_failsClosedWhenPulseUnavailable() {
        when(antennaRepository.findBySectorId(9L)).thenReturn(List.of());
        when(pulseClient.sectorCellNames("KYAMP8__9")).thenThrow(new ExternalServiceException("pulse-service", "down"));

        assertThatThrownBy(() -> sectorService.deleteSector("KYAMP8__9"))
                .isInstanceOf(ExternalServiceException.class);
        verify(sectorRepository, never()).delete(any());
    }
}
