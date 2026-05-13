package dev.thilanka.beam.dto;

public record BeamSiteDto(
        String siteCode,
        String siteName,
        Double latitude,
        Double longitude,
        Short buildingHeight,
        Short towerHeight,
        String operatorName,
        String infraType
) {
}
