package dev.thilanka.beam.dto;

public record AntennaDto(
        Long id,
        byte antennaIndex,
        short azimuth,
        short mechanicalTilt,
        short antennaHeight,
        boolean retAvailable,
        String antennaTypeName,
        String sectorName,
        String manufacturerName,
        AuditDto audit
) {
}