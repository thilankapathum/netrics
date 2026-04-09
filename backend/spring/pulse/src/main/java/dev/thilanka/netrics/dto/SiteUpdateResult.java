package dev.thilanka.netrics.dto;

import java.util.List;

public record SiteUpdateResult(
        SiteDto siteDto,
        List<String> warnings
) {
}
