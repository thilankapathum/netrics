package dev.thilanka.beam.dto;

public record AntennaDto(
        Long id,
        byte antennaIndex,
        short azimuth,
        short mechanicalTilt,
        short antennaHeight,
        String antennaTypeName,
        String sectorName,
        String manufacturerName
) {
}