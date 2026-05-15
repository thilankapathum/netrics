package dev.thilanka.beam.dto;

import java.util.List;

public record SiteUpdateResult(
        SiteDto siteDto,
        List<String> warnings
) {
}
