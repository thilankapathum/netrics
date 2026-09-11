package dev.thilanka.beam.dto;

public record ElectricalTiltDto(
        Long id,
        byte electricalTilt,
        Long antennaId,
        String bandName
) {
}
