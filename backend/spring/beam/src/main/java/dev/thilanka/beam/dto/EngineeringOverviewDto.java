package dev.thilanka.beam.dto;

import java.util.List;

public record EngineeringOverviewDto(
        Counts counts,
        Breakdowns breakdowns,
        List<Gap> gaps
) {
    public record Counts(long sites, long sectors, long antennas, long tilts) {
    }

    /** Groups sorted by count, largest first; unassigned rows are grouped as "Not set" */
    public record Breakdowns(List<NameCount> operator, List<NameCount> infraType,
                             List<NameCount> band, List<NameCount> manufacturer) {
    }

    public record NameCount(String name, long count) {
    }

    /** {@code count} counts the offending rows; {@code siteCodes} is a sample of the sites they sit on */
    public record Gap(String key, String label, long count, List<String> siteCodes) {
    }
}
