package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.ManufacturerDto;
import dev.thilanka.beam.entity.Manufacturer;

import java.util.List;

public interface ManufacturerService {

    Manufacturer createManufacturer(Manufacturer manufacturer);

    ManufacturerDto createManufacturer(ManufacturerDto dto);

    List<ManufacturerDto> createManufacturers(List<ManufacturerDto> dtos);

    Manufacturer updateManufacturer(Manufacturer manufacturer);

    ManufacturerDto updateManufacturer(ManufacturerDto dto);

    List<ManufacturerDto> updateManufacturers(List<ManufacturerDto> dtos);

    Manufacturer deleteManufacturer(String manufacturerName);

    ManufacturerDto deleteManufacturerDto(String manufacturerName);

    Manufacturer findByName(String manufacturerName);

    ManufacturerDto getByName(String manufacturerName);
}
