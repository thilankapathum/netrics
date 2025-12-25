package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.BandDto;
import dev.thilanka.netrics.entity.Band;

import java.util.List;

public interface BandService {


    Band createBand(Band band);

    BandDto createBand(BandDto dto);

    List<BandDto> createBands(List<BandDto> dtos);

    List<Band> findAll();

    List<BandDto> getAll();

    Band findByName(String name);

    BandDto getByName(String name);
}
