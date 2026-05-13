package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.entity.AntennaType;
import dev.thilanka.beam.repository.AntennaTypeRepository;
import dev.thilanka.beam.service.AntennaTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AntennaTypeServiceImpl implements AntennaTypeService {
    private final AntennaTypeRepository antennaTypeRepository;

    @Override
    public AntennaType findByName(String name) {
        return antennaTypeRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("Antenna-Type", "name", name));
    }
}
