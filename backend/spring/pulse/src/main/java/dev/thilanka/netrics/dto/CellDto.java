package dev.thilanka.netrics.dto;

public record CellDto(
        String cellName,
        String nodeName,
        String ratName,
        String siteCode,
        String bandName
) {
}
