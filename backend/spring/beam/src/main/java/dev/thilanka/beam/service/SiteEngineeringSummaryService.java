package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.SiteEngineeringSummaryDto;

public interface SiteEngineeringSummaryService {

    SiteEngineeringSummaryDto getBySiteCode(String siteCode);
}
