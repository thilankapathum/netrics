package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.CarrierDto;
import dev.thilanka.netrics.entity.Band;
import dev.thilanka.netrics.entity.Carrier;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CarrierRepository;
import dev.thilanka.netrics.service.BandService;
import dev.thilanka.netrics.service.CarrierService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository carrierRepository;
    private final RatService ratService;
    private final BandService bandService;
    private final Mapper mapper;

    @Override
    public Carrier createCarrier(Carrier carrier) {
        return carrierRepository.save(carrier);
    }

    @Override
    public CarrierDto createCarrier(CarrierDto dto) {

        Rat rat = ratService.findRatByName(dto.ratName());
        Band band = bandService.findByName(dto.bandName());

        Carrier carrier = Carrier.builder()
                .name(dto.name())
                .radius(dto.radius())
                .rat(rat)
                .band(band)
                .build();

        Carrier savedCarrier = createCarrier(carrier);

        return mapper.carrierToDto(savedCarrier);
    }

    @Override
    public Carrier findByName(String name) {
        return carrierRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("Carrier", "name", name));
    }

    @Override
    public CarrierDto getByName(String name) {
        Carrier carrier = findByName(name);
        return mapper.carrierToDto(carrier);
    }

    @Override
    public List<Carrier> findAll() {
        return carrierRepository.findAll();
    }

    @Override
    public List<CarrierDto> getAll() {
        List<Carrier> carriers = findAll();
        return carriers.stream().map(mapper::carrierToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarrierDto> createCarriers(List<CarrierDto> dtos) {

        List<CarrierDto> carrierDtos = new ArrayList<>();

        for (CarrierDto dto : dtos) {
            try {
                carrierDtos.add(createCarrier(dto));
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        return carrierDtos;
    }
}
