package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CarrierDto;
import dev.thilanka.netrics.entity.Carrier;

import java.util.List;

public interface CarrierService {

    Carrier createCarrier(Carrier carrier);

    CarrierDto createCarrier(CarrierDto dto);

    Carrier findByName(String name);

    CarrierDto getByName(String name);

    List<Carrier> findAll();

    List<CarrierDto> getAll();

    List<CarrierDto> createCarriers(List<CarrierDto> dtos);
}
