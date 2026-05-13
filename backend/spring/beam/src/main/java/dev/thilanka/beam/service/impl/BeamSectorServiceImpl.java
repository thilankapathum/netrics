package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.entity.BeamSector;
import dev.thilanka.beam.repository.BeamSectorRepository;
import dev.thilanka.beam.service.BeamSectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeamSectorServiceImpl implements BeamSectorService {
    private final BeamSectorRepository beamSectorRepository;

    @Override
    public BeamSector findByName(String name) {
        return beamSectorRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("Beam", "name", name));
    }
}
