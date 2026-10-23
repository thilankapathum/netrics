package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.EngineeringOverviewDto;
import dev.thilanka.beam.dto.OperationLogDto;

import java.util.List;

public interface EngineeringParameterOverviewService {

    /** Network-wide counts, breakdowns and data gaps for the Site Parameters landing page */
    EngineeringOverviewDto overview();

    /** Latest changes to sites, sectors, antennas and tilts, newest first */
    List<OperationLogDto> changes(int page, int size);
}
