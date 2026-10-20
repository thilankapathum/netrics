package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.AuditDto;
import dev.thilanka.beam.dto.ElectricalTiltDto;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.BeamBand;
import dev.thilanka.beam.entity.ElectricalTilt;
import dev.thilanka.beam.repository.ElectricalTiltRepository;
import dev.thilanka.beam.service.AntennaService;
import dev.thilanka.beam.service.BeamBandService;
import dev.thilanka.beam.service.ElectricalTiltService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectricalTiltServiceImpl implements ElectricalTiltService {
    private final ElectricalTiltRepository electricalTiltRepository;
    private final AntennaService antennaService;
    private final BeamBandService beamBandService;

    @Override
    public ElectricalTilt createElectricalTilt(ElectricalTilt electricalTilt) {
        return electricalTiltRepository.save(electricalTilt);
    }

    @Override
    public ElectricalTiltDto createElectricalTilt(ElectricalTiltDto dto) {
        return toDto(createElectricalTilt(toEntity(dto)));
    }

    @Override
    public List<ElectricalTiltDto> createElectricalTilts(List<ElectricalTiltDto> dtos) {
        List<ElectricalTiltDto> electricalTiltDtos = new ArrayList<>();

        for (ElectricalTiltDto dto : dtos) {
            try {
                electricalTiltDtos.add(createElectricalTilt(dto));
            } catch (Exception e) {
                log.warn("Error creating Electrical-Tilt antennaId={}", dto.antennaId(), e);
            }
        }
        return electricalTiltDtos;
    }

    @Override
    public ElectricalTilt updateElectricalTilt(ElectricalTilt electricalTilt) {
        ElectricalTilt existingElectricalTilt = findById(electricalTilt.getId());

        existingElectricalTilt.setElectricalTilt(electricalTilt.getElectricalTilt());
        existingElectricalTilt.setAntenna(electricalTilt.getAntenna());
        existingElectricalTilt.setBand(electricalTilt.getBand());

        return electricalTiltRepository.save(existingElectricalTilt);
    }

    @Override
    public ElectricalTiltDto updateElectricalTilt(ElectricalTiltDto dto) {
        return toDto(updateElectricalTilt(toEntity(dto)));
    }

    @Override
    public List<ElectricalTiltDto> updateElectricalTilts(List<ElectricalTiltDto> dtos) {
        List<ElectricalTiltDto> electricalTiltDtos = new ArrayList<>();

        for (ElectricalTiltDto dto : dtos) {
            try {
                electricalTiltDtos.add(updateElectricalTilt(dto));
            } catch (Exception e) {
                log.warn("Error updating Electrical-Tilt id={}", dto.id(), e);
            }
        }
        return electricalTiltDtos;
    }

    @Override
    public void deleteElectricalTilt(Long id) {
        electricalTiltRepository.deleteById(id);
    }

    @Override
    public ElectricalTilt findById(Long id) {
        return electricalTiltRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electrical-Tilt", "id", id));
    }

    @Override
    public ElectricalTiltDto getById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public List<ElectricalTilt> findByAntennaId(Long antennaId) {
        return electricalTiltRepository.findByAntennaId(antennaId);
    }

    @Override
    public List<ElectricalTiltDto> getByAntennaId(Long antennaId) {
        return findByAntennaId(antennaId).stream().map(this::toDto).toList();
    }

    private ElectricalTiltDto toDto(ElectricalTilt electricalTilt) {
        return new ElectricalTiltDto(
                electricalTilt.getId(),
                electricalTilt.getElectricalTilt(),
                electricalTilt.getAntenna().getId(),
                electricalTilt.getBand().getName(),
                AuditDto.of(electricalTilt)
        );
    }

    private ElectricalTilt toEntity(ElectricalTiltDto dto) {
        Antenna antenna = antennaService.findById(dto.antennaId());
        BeamBand band = beamBandService.findByName(dto.bandName());

        return ElectricalTilt.builder()
                .id(dto.id())
                .electricalTilt(dto.electricalTilt())
                .antenna(antenna)
                .band(band)
                .build();
    }
}
