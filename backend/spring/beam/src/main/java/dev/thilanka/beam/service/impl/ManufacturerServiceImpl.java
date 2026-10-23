package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.Mapper;
import dev.thilanka.beam.common.exception.DataNotChangedException;
import dev.thilanka.beam.common.exception.DependencyConflictException;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.ManufacturerDto;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.Manufacturer;
import dev.thilanka.beam.repository.AntennaRepository;
import dev.thilanka.beam.repository.ManufacturerRepository;
import dev.thilanka.beam.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {
    private final ManufacturerRepository manufacturerRepository;
    private final AntennaRepository antennaRepository;
    private final Mapper mapper;

    @Override
    public Manufacturer createManufacturer(Manufacturer manufacturer) {
        return manufacturerRepository.save(manufacturer);
    }

    @Override
    public ManufacturerDto createManufacturer(ManufacturerDto dto) {
        return mapper.toManufacturerDto(createManufacturer(dtoToManufacturer(dto)));
    }

    @Override
    public List<ManufacturerDto> createManufacturers(List<ManufacturerDto> dtos) {
        List<ManufacturerDto> manufacturerDtos = new ArrayList<>();

        for (ManufacturerDto dto : dtos) {
            try {
                manufacturerDtos.add(createManufacturer(dto));
            } catch (Exception e) {
                log.warn("Error creating Manufacturer by name={}", dto.name(), e);
            }
        }
        return manufacturerDtos;
    }

    @Override
    public Manufacturer updateManufacturer(Manufacturer manufacturer) {

        Manufacturer existingManufacturer = manufacturerRepository.findById(manufacturer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer", "id", manufacturer.getId()));
        if (existingManufacturer.getName().equals(manufacturer.getName())) {
            throw new DataNotChangedException("Manufacturer", "name", manufacturer.getName());
        }
        existingManufacturer.setName(manufacturer.getName());
        return manufacturerRepository.save(existingManufacturer);
    }

    @Override
    public ManufacturerDto updateManufacturer(ManufacturerDto dto) {
        Manufacturer manufacturer = dtoToManufacturer(dto);
        return mapper.toManufacturerDto(updateManufacturer(manufacturer));
    }

    @Override
    public List<ManufacturerDto> updateManufacturers(List<ManufacturerDto> dtos) {
        List<ManufacturerDto> manufacturerDtos = new ArrayList<>();

        for (ManufacturerDto dto : dtos) {
            manufacturerDtos.add(updateManufacturer(dto));
        }
        return manufacturerDtos;
    }

    @Override
    @Transactional
    public Manufacturer deleteManufacturer(String manufacturerName, boolean force) {
        Manufacturer existingManufacturer = findByName(manufacturerName);

        List<Antenna> dependents = antennaRepository.findByManufacturer_Id(existingManufacturer.getId());
        if (!dependents.isEmpty()) {
            if (!force) {
                throw new DependencyConflictException("Manufacturer", manufacturerName, dependents.size());
            }
            antennaRepository.nullifyManufacturer(existingManufacturer.getId());
        }

        manufacturerRepository.deleteByIdNative(existingManufacturer.getId());
        return existingManufacturer;
    }

    @Override
    public ManufacturerDto deleteManufacturerDto(String manufacturerName, boolean force) {
        return mapper.toManufacturerDto(deleteManufacturer(manufacturerName, force));
    }

    @Override
    public List<String> getDependentAntennaLabels(String manufacturerName) {
        Manufacturer manufacturer = findByName(manufacturerName);
        return antennaRepository.findByManufacturer_Id(manufacturer.getId()).stream()
                .map(a -> a.getSector().getName() + " / Antenna " + a.getAntennaIndex())
                .toList();
    }

    @Override
    public Manufacturer findByName(String manufacturerName) {
        return manufacturerRepository.findByName(manufacturerName)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer", "name", manufacturerName));
    }

    @Override
    public ManufacturerDto getByName(String manufacturerName) {
        return mapper.toManufacturerDto(findByName(manufacturerName));
    }

    @Override
    public List<Manufacturer> findAll() {
        return manufacturerRepository.findAll();
    }

    @Override
    public List<ManufacturerDto> getAll() {
        return findAll().stream().map(mapper::toManufacturerDto).toList();
    }

    private Manufacturer dtoToManufacturer(ManufacturerDto dto) {
        return Manufacturer.builder()
                .id(dto.id())
                .name(dto.name())
                .build();
    }
}
