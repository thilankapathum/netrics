package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.EngineeringOverviewDto;
import dev.thilanka.beam.dto.OperationLogDto;

import java.util.List;

public interface EngineeringParameterOverviewService {

    /** Counts, breakdowns and data gaps over the sites in a pulse area, for the Site Parameters landing page */
    EngineeringOverviewDto overview(String areaName);

    /** Latest changes to sites, sectors, antennas and tilts, newest first */
    List<OperationLogDto> changes(int page, int size);
}
