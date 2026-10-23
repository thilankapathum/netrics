package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.ElectricalTiltDto;
import dev.thilanka.beam.entity.ElectricalTilt;

import java.util.List;

public interface ElectricalTiltService {

    ElectricalTilt createElectricalTilt(ElectricalTilt electricalTilt);

    ElectricalTiltDto createElectricalTilt(ElectricalTiltDto dto);

    List<ElectricalTiltDto> createElectricalTilts(List<ElectricalTiltDto> dtos);

    ElectricalTilt updateElectricalTilt(ElectricalTilt electricalTilt);

    ElectricalTiltDto updateElectricalTilt(ElectricalTiltDto dto);

    List<ElectricalTiltDto> updateElectricalTilts(List<ElectricalTiltDto> dtos);

    void deleteElectricalTilt(Long id);

    ElectricalTilt findById(Long id);

    ElectricalTiltDto getById(Long id);

    List<ElectricalTilt> findByAntennaId(Long antennaId);

    List<ElectricalTiltDto> getByAntennaId(Long antennaId);
}
