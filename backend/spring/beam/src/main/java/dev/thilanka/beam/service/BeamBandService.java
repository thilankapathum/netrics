package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.BeamBandDto;
import dev.thilanka.beam.entity.BeamBand;

import java.util.List;

public interface BeamBandService {

    BeamBand findByName(String name);
    BeamBandDto getByName(String name);
    List<BeamBand> findAll();
    List<BeamBandDto> getAll();

}
