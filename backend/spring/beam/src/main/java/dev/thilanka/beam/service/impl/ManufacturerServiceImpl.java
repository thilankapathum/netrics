package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.Mapper;
import dev.thilanka.beam.common.exception.DataNotChangedException;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.ManufacturerDto;
import dev.thilanka.beam.entity.Manufacturer;
import dev.thilanka.beam.repository.ManufacturerRepository;
import dev.thilanka.beam.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {
    private final ManufacturerRepository manufacturerRepository;
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

        Manufacturer existingManufacturer = findByName(manufacturer.getName());
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
    public Manufacturer deleteManufacturer(String manufacturerName) {
        Manufacturer existingManufacturer = findByName(manufacturerName);
        if (existingManufacturer == null) {
            return null;
        }
        manufacturerRepository.delete(existingManufacturer);
        return existingManufacturer;
    }

    @Override
    public ManufacturerDto deleteManufacturerDto(String manufacturerName) {
        return mapper.toManufacturerDto(deleteManufacturer(manufacturerName));
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

    private Manufacturer dtoToManufacturer(ManufacturerDto dto) {
        return Manufacturer.builder()
                .name(dto.name())
                .build();
    }
}
