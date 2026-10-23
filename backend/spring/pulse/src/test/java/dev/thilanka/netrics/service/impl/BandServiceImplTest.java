package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.assistant.BeamClient;
import dev.thilanka.netrics.common.exception.BusinessValidationException;
import dev.thilanka.netrics.common.exception.ExternalServiceException;
import dev.thilanka.netrics.dto.BandEvent;
import dev.thilanka.netrics.entity.Band;
import dev.thilanka.netrics.repository.BandRepository;
import dev.thilanka.netrics.repository.CarrierRepository;
import dev.thilanka.netrics.repository.CellRepository;
import dev.thilanka.netrics.service.PulseEventPublisher;
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
class BandServiceImplTest {

    @Mock
    private BandRepository bandRepository;
    @Mock
    private CellRepository cellRepository;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private BeamClient beamClient;
    @Mock
    private PulseEventPublisher eventPublisher;

    @InjectMocks
    private BandServiceImpl bandService;

    private final Band band = Band.builder().id(7L).name("L18").number(3).unit("MHz").build();

    @BeforeEach
    void setUp() {
        when(bandRepository.findByName("L18")).thenReturn(Optional.of(band));
        lenient().when(carrierRepository.findByBand_Id(7L)).thenReturn(List.of());
    }

    @Test
    void deleteBand_blockedWhenCellsUseIt() {
        when(cellRepository.countByBand_Id(7L)).thenReturn(12L);
        when(beamClient.bandTiltCount("L18")).thenReturn(0L);

        assertThatThrownBy(() -> bandService.deleteBand("L18"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("12 cell(s)");
        verify(bandRepository, never()).delete(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deleteBand_blockedWhenBeamUnavailable() {
        when(cellRepository.countByBand_Id(7L)).thenReturn(0L);
        when(beamClient.bandTiltCount("L18")).thenThrow(new ExternalServiceException("beam-service", "down"));

        assertThatThrownBy(() -> bandService.deleteBand("L18")).isInstanceOf(ExternalServiceException.class);
        verify(bandRepository, never()).delete(any());
    }

    @Test
    void deleteBand_deletesAndPublishesWhenUnused() {
        when(cellRepository.countByBand_Id(7L)).thenReturn(0L);
        when(beamClient.bandTiltCount("L18")).thenReturn(0L);

        bandService.deleteBand("L18");

        verify(bandRepository).delete(band);
        ArgumentCaptor<BandEvent> event = ArgumentCaptor.forClass(BandEvent.class);
        verify(eventPublisher).publishBandEvent(event.capture());
        assertThat(event.getValue().eventType()).isEqualTo("DELETED");
        assertThat(event.getValue().id()).isEqualTo(7L);
    }
}
