package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BandDto;
import dev.thilanka.netrics.entity.Band;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.BandRepository;
import dev.thilanka.netrics.service.BandService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BandServiceImpl implements BandService {
    private final BandRepository bandRepository;
    private final RatService ratService;

    @Override
    public Band createBand(Band band) {
        return bandRepository.save(band);
    }

    @Override
    public BandDto createBand(BandDto dto) {

        Band band = Band.builder()
                .name(dto.name())
                .number(dto.number())
                .unit(dto.unit())
                .build();

        Band savedBand = createBand(band);

        return new BandDto(
                savedBand.getName(),
                savedBand.getNumber(),
                savedBand.getUnit()
        );
    }

    @Override
    public List<BandDto> createBands(List<BandDto> dtos) {

        List<BandDto> savedBandDtos = new ArrayList<>();

        for (BandDto dto: dtos){
            BandDto savedBandDto = createBand(dto);
            savedBandDtos.add(savedBandDto);
        }

        return savedBandDtos;
    }

    @Override
    public List<Band> findAll() {
        return bandRepository.findAll();
    }

    @Override
    public List<BandDto> getAll() {
        List<Band> allBands = findAll();

        return allBands
                .stream()
                .map(b -> new BandDto(b.getName(),b.getNumber(),b.getUnit()))
                .collect(Collectors.toList());
    }

    @Override
    public Band findByName(String name) {

        return bandRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Band not found by " + name));
    }

    @Override
    public BandDto getByName(String name) {

        Band band = findByName(name);

        return new BandDto(
                band.getName(),
                band.getNumber(),
                band.getUnit()
        );
    }

    @Override
    public List<BandDto> getBandsByRat(String ratName) {
        Rat rat = ratService.findRatByName(ratName);

        List<Band> bands = bandRepository.findBandsByRat(rat.getId());

        return bands.stream().map(b -> new BandDto(b.getName(),b.getNumber(),b.getUnit()))
                .collect(Collectors.toList());
    }
}
