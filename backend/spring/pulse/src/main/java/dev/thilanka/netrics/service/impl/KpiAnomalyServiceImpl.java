package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiAnomalyDto;
import dev.thilanka.netrics.dto.KpiAnomalyProjection;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.KpiAnomalyRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.GranularityService;
import dev.thilanka.netrics.service.KpiAnomalyService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiAnomalyServiceImpl implements KpiAnomalyService {

    private final KpiAnomalyRepository kpiAnomalyRepository;
    private final RatService ratService;
    private final GranularityService granularityService;
    private final KpiDayRepository kpiDayRepository;

    private static final Map<String, Integer> SEVERITY_RANK = Map.of(
            "moderate", 1, "high", 2, "critical", 3
    );

    @Override
    public List<KpiAnomalyDto> getLatestAnomalies(String ratName, String granularityName, String minSeverity) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime latestDate = kpiDayRepository.getLatestDate(rat.getId(), granularity.getId());
        if (latestDate == null) {
            return List.of();
        }
        LocalDateTime currStart = latestDate.toLocalDate().atStartOfDay();
        LocalDateTime currEnd = currStart.plusSeconds(granularity.getPlusSeconds());

        List<KpiAnomalyProjection> anomalies =
                kpiAnomalyRepository.findAnomaliesByDateRange(currStart, currEnd, rat.getId(), granularity.getId());

        int minRank = minSeverity != null
                ? SEVERITY_RANK.getOrDefault(minSeverity.toLowerCase(), 1)
                : 1;

        return anomalies.stream()
                .filter(a -> SEVERITY_RANK.getOrDefault(a.getSeverity(), 1) >= minRank)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private KpiAnomalyDto toDto(KpiAnomalyProjection p) {
        return new KpiAnomalyDto(
                p.getCellName(), p.getKpiName(), p.getKpiLabel(), p.getUnit(),
                p.getTimestamp(), p.getObservedValue(), p.getBaselineMedian(),
                p.getMad(), p.getRobustZScore(), p.getSeverity()
        );
    }
}
