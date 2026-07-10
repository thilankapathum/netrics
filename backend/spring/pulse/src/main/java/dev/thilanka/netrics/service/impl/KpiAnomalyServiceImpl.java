package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.repository.KpiAnomalyRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final AreaService areaService;
    private final StandardKpiService standardKpiService;
    private final DateService dateService;

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

    @Override
    @Cacheable(value = "anomalyCells", key = "#period + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public List<WorstCellsDto> getAllAnomalyCellsByArea(String period, String areaName, String ratName, String granularityName) {

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        Area area = areaService.findAreaByName(areaName);

        LocalDateTime latestDate = kpiDayRepository.getLatestDate(rat.getId(), granularity.getId());
        LocalDateTime currStart = latestDate.toLocalDate().atStartOfDay();
        LocalDateTime currEnd = currStart.plusSeconds(granularity.getPlusSeconds());

        int prevPeriodDays = dateService.getPeriod(period).intValue();
        LocalDateTime streakStart = currEnd.toLocalDate().minusDays(30).atStartOfDay();

        List<AnomalyCellsProjection> anomalyCells = kpiAnomalyRepository.findAllAnomalyCellsByArea(
                currStart, currEnd, prevPeriodDays, area.getId(), rat.getId(), granularity.getId()
        );

        if (anomalyCells.isEmpty()) return List.of();

        // Group by KPI name so streaks are looked up correctly per-KPI, not mixed across KPIs
        Map<String, List<AnomalyCellsProjection>> byKpi = anomalyCells.stream()
                .collect(Collectors.groupingBy(AnomalyCellsProjection::kpiName));

        Map<String, Integer> streaks = new HashMap<>();
        for (Map.Entry<String, List<AnomalyCellsProjection>> entry : byKpi.entrySet()) {
            StandardKpi standardKpi = standardKpiService.findByKpiName(entry.getKey(), rat);
            List<String> cellNames = entry.getValue().stream()
                    .map(AnomalyCellsProjection::cellName)
                    .toList();

            // Key streaks by "cellName|kpiName" since the same cell can appear under multiple KPIs
            kpiDayRepository.findStreaksForCells(
                            standardKpi.getId(), streakStart, currEnd,
                            cellNames.toArray(String[]::new),
                            rat.getId(), granularity.getId()
                    )
                    .forEach(s -> streaks.put(s.cellName() + "|" + entry.getKey(), s.consecutiveBadDays()));
        }

        return anomalyCells.stream()
                .map(ac -> new WorstCellsDto(
                        ac.cellName(),
                        ac.kpiName(),
                        ac.kpiLabel(),
                        ac.unit(),
                        ac.value(),
                        ac.previousValue(),
                        ac.difference(),
                        ac.improved(),
                        streaks.getOrDefault(ac.cellName() + "|" + ac.kpiName(), 0),
                        ac.severity()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<WorstCellsDto> getAllAnomalyCellsByArea(String period, String areaName, String ratName, String granularityName, String severity, String sortBy, String sortDir, int page, int pageSize) {

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        Area area = areaService.findAreaByName(areaName);

        LocalDateTime latestDate = kpiAnomalyRepository.getLatestDate(rat.getId(), granularity.getId());
        LocalDateTime currStart = latestDate.toLocalDate().atStartOfDay();
        LocalDateTime currEnd = currStart.plusSeconds(granularity.getPlusSeconds());

        int prevPeriodDays = dateService.getPeriod(period).intValue();
        LocalDateTime streakStart = currEnd.toLocalDate().minusDays(30).atStartOfDay();

        long totalElements = kpiAnomalyRepository.countAnomalyCellsByArea(
                currStart, currEnd, area.getId(), rat.getId(), granularity.getId(), severity);

        if (totalElements == 0) {
            return new PagedResponse<>(List.of(), 0, 0, page, pageSize);
        }

        List<AnomalyCellsProjection> anomalyCells = kpiAnomalyRepository.findAllAnomalyCellsByAreaPaged(
                currStart, currEnd, prevPeriodDays, area.getId(), rat.getId(), granularity.getId(),
                severity, sortBy, sortDir, pageSize, page * pageSize
        );

        Map<String, List<AnomalyCellsProjection>> byKpi = anomalyCells.stream()
                .collect(Collectors.groupingBy(AnomalyCellsProjection::kpiName));

        Map<String, Integer> streaks = new HashMap<>();
        for (Map.Entry<String, List<AnomalyCellsProjection>> entry : byKpi.entrySet()) {
            StandardKpi standardKpi = standardKpiService.findByKpiName(entry.getKey(), rat);
            List<String> cellNames = entry.getValue().stream()
                    .map(AnomalyCellsProjection::cellName)
                    .toList();

            kpiDayRepository.findStreaksForCells(
                            standardKpi.getId(), streakStart, currEnd,
                            cellNames.toArray(String[]::new),
                            rat.getId(), granularity.getId()
                    )
                    .forEach(s -> streaks.put(s.cellName() + "|" + entry.getKey(), s.consecutiveBadDays()));
        }

        List<WorstCellsDto> content = anomalyCells.stream()
                .map(ac -> new WorstCellsDto(
                        ac.cellName(), ac.kpiName(), ac.kpiLabel(), ac.unit(),
                        ac.value(), ac.previousValue(), ac.difference(), ac.improved(),
                        streaks.getOrDefault(ac.cellName() + "|" + ac.kpiName(), 0),
                        ac.severity()
                ))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new PagedResponse<>(content, totalElements, totalPages, page, pageSize);
    }

    private KpiAnomalyDto toDto(KpiAnomalyProjection p) {
        return new KpiAnomalyDto(
                p.getCellName(), p.getKpiName(), p.getKpiLabel(), p.getUnit(),
                p.getTimestamp(), p.getObservedValue(), p.getBaselineMedian(),
                p.getMad(), p.getRobustZScore(), p.getSeverity()
        );
    }
}
