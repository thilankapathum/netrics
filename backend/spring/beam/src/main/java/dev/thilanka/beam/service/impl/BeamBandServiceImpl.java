package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.BeamBandDto;
import dev.thilanka.beam.entity.BeamBand;
import dev.thilanka.beam.repository.BeamBandRepository;
import dev.thilanka.beam.service.BeamBandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeamBandServiceImpl implements BeamBandService {
    private final BeamBandRepository beamBandRepository;


    @Override
    public BeamBand findByName(String name) {
        return beamBandRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("Band", "name", name));
    }

    @Override
    public BeamBandDto getByName(String name) {
        return toDto(findByName(name));
    }

    @Override
    public List<BeamBand> findAll() {
        return beamBandRepository.findAll();
    }

    @Override
    public List<BeamBandDto> getAll() {
        return findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    BeamBandDto toDto(BeamBand band) {
        return new BeamBandDto(
                band.getId(),
                band.getName(),
                band.getNumber(),
                band.getUnit()
        );
    }
}
