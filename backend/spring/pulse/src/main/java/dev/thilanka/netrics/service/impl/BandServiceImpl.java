package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.assistant.BeamClient;
import dev.thilanka.netrics.common.exception.BusinessValidationException;
import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.BandDto;
import dev.thilanka.netrics.dto.BandEvent;
import dev.thilanka.netrics.entity.Band;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.BandRepository;
import dev.thilanka.netrics.repository.CarrierRepository;
import dev.thilanka.netrics.repository.CellRepository;
import dev.thilanka.netrics.service.BandService;
import dev.thilanka.netrics.service.PulseEventPublisher;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BandServiceImpl implements BandService {
    private final BandRepository bandRepository;
    private final RatService ratService;
    private final PulseEventPublisher eventPublisher;
    private final CellRepository cellRepository;
    private final CarrierRepository carrierRepository;
    private final BeamClient beamClient;

    @Override
    public Band createBand(Band band) {
        Band savedBand = bandRepository.save(band);
        eventPublisher.publishBandEvent(buildBandEvent("CREATED", savedBand));
        return savedBand;
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
                .orElseThrow(()-> new ResourceNotFoundException("Band", "Name", name));
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

    @Override
    public BandDto updateBand(String name, BandDto dto) {
        Band band = findByName(name);
        band.setName(dto.name());
        band.setNumber(dto.number());
        band.setUnit(dto.unit());

        Band savedBand = bandRepository.save(band);
        eventPublisher.publishBandEvent(buildBandEvent("UPDATED", savedBand));
        return new BandDto(savedBand.getName(), savedBand.getNumber(), savedBand.getUnit());
    }

    @Override
    public List<String> getDependents(String name) {
        Band band = findByName(name);
        List<String> dependents = new ArrayList<>();

        long cellCount = cellRepository.countByBand_Id(band.getId());
        if (cellCount > 0) dependents.add(String.format("%,d cell(s)", cellCount));

        carrierRepository.findByBand_Id(band.getId())
                .forEach(c -> dependents.add("Carrier " + c.getName()));

        long tiltCount = beamClient.bandTiltCount(band.getName());
        if (tiltCount > 0) dependents.add(String.format("%,d electrical tilt(s) in Beam", tiltCount));

        return dependents;
    }

    @Override
    @Transactional
    public BandDto deleteBand(String name) {
        List<String> dependents = getDependents(name);
        if (!dependents.isEmpty()) {
            throw new BusinessValidationException(
                    "Band " + name + " is in use and cannot be deleted: " + String.join(", ", dependents),
                    "BAND_IN_USE");
        }

        Band band = findByName(name);
        bandRepository.delete(band);
        eventPublisher.publishBandEvent(buildBandEvent("DELETED", band));
        return new BandDto(band.getName(), band.getNumber(), band.getUnit());
    }

    private BandEvent buildBandEvent(String type, Band band) {
        return new BandEvent(
                type,
                band.getId(),
                band.getName(),
                band.getNumber(),
                band.getUnit(),
                band.getCreatedAt(),
                band.getCreatedBy()
        );
    }
}
