package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.Mapper;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.AntennaDto;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.AntennaType;
import dev.thilanka.beam.entity.Sector;
import dev.thilanka.beam.entity.Manufacturer;
import dev.thilanka.beam.repository.AntennaRepository;
import dev.thilanka.beam.service.AntennaService;
import dev.thilanka.beam.service.AntennaTypeService;
import dev.thilanka.beam.service.SectorService;
import dev.thilanka.beam.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AntennaServiceImpl implements AntennaService {
    private final AntennaRepository antennaRepository;
    private final AntennaTypeService antennaTypeService;
    private final SectorService sectorService;
    private final ManufacturerService manufacturerService;
    private final Mapper mapper;

    @Override
    public Antenna createAntenna(Antenna antenna) {
        return antennaRepository.save(antenna);
    }

    @Override
    public AntennaDto createAntenna(AntennaDto dto) {
        Antenna antenna = dtoToAntenna(dto);
        Antenna savedAntenna = createAntenna(antenna);
        return mapper.toAntennaDto(savedAntenna);
    }

    @Override
    public List<AntennaDto> createAntennas(List<AntennaDto> dtos) {

        List<AntennaDto> antennaDtos = new ArrayList<>();

        for (AntennaDto dto : dtos) {
            try {
                antennaDtos.add(createAntenna(dto));
            } catch (Exception e) {
                log.warn("Error creating Antenna index={} sector={}", dto.antennaIndex(), dto.sectorName(), e);
            }
        }
        return antennaDtos;
    }

    @Override
    public Antenna updateAntenna(Antenna antenna) {
        Antenna existingAntenna = antennaRepository.findById(antenna.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Antenna", "id", antenna.getId()));

        existingAntenna.setAntennaIndex(antenna.getAntennaIndex());
        existingAntenna.setAzimuth(antenna.getAzimuth());
        existingAntenna.setMechanicalTilt(antenna.getMechanicalTilt());
        existingAntenna.setAntennaHeight(antenna.getAntennaHeight());
        existingAntenna.setAntennaType(antenna.getAntennaType());
        existingAntenna.setSector(antenna.getSector());
        existingAntenna.setManufacturer(antenna.getManufacturer());

        return antennaRepository.save(existingAntenna);
    }

    @Override
    public AntennaDto updateAntenna(AntennaDto dto) {
        Antenna antenna = dtoToAntenna(dto);
        Antenna updated = updateAntenna(antenna);
        return mapper.toAntennaDto(updated);
    }

    @Override
    public List<AntennaDto> updateAntennas(List<AntennaDto> dtos) {
        List<AntennaDto> antennaDtos = new ArrayList<>();

        for (AntennaDto dto : dtos) {
            try {
                antennaDtos.add(updateAntenna(dto));
            } catch (Exception e) {
                log.warn("Error updating Antenna index={} sector={}", dto.antennaIndex(), dto.sectorName(), e);
            }
        }
        return antennaDtos;
    }

    @Override
    public Antenna deleteAntenna(Antenna antenna) {
        antennaRepository.deleteById(antenna.getId());
        return antenna;
    }

    @Override
    public AntennaDto deleteAntenna(AntennaDto dto) {
        Antenna antenna = dtoToAntenna(dto);
        return mapper.toAntennaDto(deleteAntenna(antenna));
    }

    @Override
    public Antenna findById(Long id) {
        return antennaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Antenna", "id", id));
    }

    @Override
    public AntennaDto getById(Long id) {
        return mapper.toAntennaDto(findById(id));
    }

    @Override
    public List<Antenna> findBySectorName(Sector sector) {
        return antennaRepository.findBySectorId(sector.getId());
    }

    @Override
    public List<AntennaDto> getBySectorName(String sectorName) {
        Sector sector = sectorService.findByName(sectorName);
        List<Antenna> antennas = findBySectorName(sector);
        return antennas.stream().map(mapper::toAntennaDto).collect(Collectors.toList());
    }

    private Antenna dtoToAntenna(AntennaDto dto) {
        AntennaType antennaType = antennaTypeService.findByName(dto.antennaTypeName());
        Sector sector = sectorService.findByName(dto.sectorName());
        Manufacturer manufacturer = manufacturerService.findByName(dto.manufacturerName());

        return Antenna.builder()
                .id(dto.id())
                .antennaIndex(dto.antennaIndex())
                .azimuth(dto.azimuth())
                .mechanicalTilt(dto.mechanicalTilt())
                .antennaHeight(dto.antennaHeight())
                .antennaType(antennaType)
                .sector(sector)
                .manufacturer(manufacturer)
                .build();
    }
}
