package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.KpiAnomalyRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    // Valid values for the alarmCorrelation filter param — anything else is
    // treated as "no filter" (same behavior as null/absent).
    private static final Set<String> VALID_ALARM_CORRELATION_FILTERS = Set.of("correlated", "uncorrelated");

    private String normalizeAlarmCorrelationFilter(String raw) {
        if (raw == null || raw.isBlank() || !VALID_ALARM_CORRELATION_FILTERS.contains(raw.toLowerCase())) {
            return null;
        }
        return raw.toLowerCase();
    }

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
    @Transactional
    public List<WorstCellsDto> getAllAnomalyCellsByArea(String period, String areaName, String ratName, String granularityName) {
        kpiAnomalyRepository.setWorkMem64();

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

        Map<String, Integer> streaks = fetchStreaks(anomalyCells, streakStart, currEnd, rat.getId(), granularity.getId());

        return anomalyCells.stream()
                .map(ac -> new WorstCellsDto(
                        ac.getTimestamp(),
                        ac.getCellName(),
                        ac.getKpiName(),
                        ac.getKpiLabel(),
                        ac.getUnit(),
                        ac.getValue(),
                        ac.getPreviousValue(),
                        ac.getDifference(),
                        ac.getImproved(),
                        streaks.getOrDefault(ac.getCellName() + "|" + ac.getStandardKpiId(), 0),
                        ac.getSeverity(),
                        ac.getHasAlarmCorrelation(),
                        ac.getDistinctAlarmDefCount(),
                        ac.getTotalAlarmOccurrences(),
                        ac.getBestMatchLevel()
                ))
                .collect(Collectors.toList());
    }

    // Batches the "consecutive bad days" streak lookup across every (cellName, standardKpiId) pair
    // in one query instead of one query per distinct KPI — avoids an N+1 pattern that dominated
    // worst-cells page latency when a page spans many distinct anomalous KPIs.
    private record CellKpiPair(String cellName, Long standardKpiId) {}

    private Map<String, Integer> fetchStreaks(List<AnomalyCellsProjection> anomalyCells,
                                               LocalDateTime streakStart, LocalDateTime currEnd,
                                               Long ratId, Long granularityId) {
        List<CellKpiPair> distinctPairs = anomalyCells.stream()
                .map(ac -> new CellKpiPair(ac.getCellName(), ac.getStandardKpiId()))
                .distinct()
                .toList();

        Map<String, Integer> streaks = new HashMap<>();
        kpiDayRepository.findStreaksForCellKpiPairs(
                        distinctPairs.stream().map(CellKpiPair::cellName).toArray(String[]::new),
                        distinctPairs.stream().map(CellKpiPair::standardKpiId).toArray(Long[]::new),
                        streakStart, currEnd, ratId, granularityId
                )
                .forEach(s -> streaks.put(s.cellName() + "|" + s.standardKpiId(), s.consecutiveBadDays()));
        return streaks;
    }

    @Override
    @Cacheable(value = "allAnomalyCellsByArea", key = "#period + '_' + #areaName + '_' + #severity + '_' + #kpiName + '_' + #sortBy + '_' + #sortDir + '_' + #page + '_' + #pageSize + '_' + #alarmCorrelation + '_' + #granularityName + '_' + #ratName")
    @Transactional
    public PagedResponse<WorstCellsDto> getAllAnomalyCellsByArea(
            String period, String areaName, String ratName, String granularityName,
            String severity, String kpiName, String sortBy, String sortDir, int page, int pageSize, String alarmCorrelation) {
        kpiAnomalyRepository.setWorkMem64();

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        Area area = areaService.findAreaByName(areaName);
        String alarmCorrelationFilter = normalizeAlarmCorrelationFilter(alarmCorrelation);

        Long standardKpiId = null;
        if (kpiName != null && !kpiName.isBlank() && !kpiName.equalsIgnoreCase("all")) {
            standardKpiId = standardKpiService.findByKpiName(kpiName, rat).getId();
        }

        LocalDateTime latestDate = kpiAnomalyRepository.getLatestDate(rat.getId(), granularity.getId());
        LocalDateTime currStart = latestDate.toLocalDate().atStartOfDay();
        LocalDateTime currEnd = currStart.plusSeconds(granularity.getPlusSeconds());

        LocalDateTime prevStart = dateService.getPreviousDate(currStart, period);
        LocalDateTime prevEnd = dateService.getPreviousDate(currEnd, period);

        LocalDateTime streakStart = currEnd.toLocalDate().minusDays(30).atStartOfDay();

        long totalElements = kpiAnomalyRepository.countAnomalyCellsByArea(
                currStart, currEnd, area.getId(), rat.getId(), granularity.getId(), severity, standardKpiId, alarmCorrelationFilter);

        if (totalElements == 0) {
            return new PagedResponse<>(List.of(), 0, 0, page, pageSize);
        }

        List<AnomalyCellsProjection> anomalyCells = kpiAnomalyRepository.findAllAnomalyCellsByAreaPaged(
                currStart, currEnd, prevStart, prevEnd, area.getId(), rat.getId(), granularity.getId(),
                severity, standardKpiId, sortBy, sortDir, pageSize, page * pageSize, alarmCorrelationFilter
        );

        Map<String, Integer> streaks = fetchStreaks(anomalyCells, streakStart, currEnd, rat.getId(), granularity.getId());

        List<WorstCellsDto> content = anomalyCells.stream()
                .map(ac -> new WorstCellsDto(
                        ac.getTimestamp(), ac.getCellName(), ac.getKpiName(), ac.getKpiLabel(), ac.getUnit(),
                        ac.getValue(), ac.getPreviousValue(), ac.getDifference(), ac.getImproved(),
                        streaks.getOrDefault(ac.getCellName() + "|" + ac.getStandardKpiId(), 0),
                        ac.getSeverity(),
                        ac.getHasAlarmCorrelation(),
                        ac.getDistinctAlarmDefCount(),
                        ac.getTotalAlarmOccurrences(),
                        ac.getBestMatchLevel()
                ))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new PagedResponse<>(content, totalElements, totalPages, page, pageSize);
    }

    @Override
    @Cacheable(value = "anomalySummaryByArea", key = "#kpiName + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public AnomalySummaryDto getAnomalySummaryByArea(String areaName, String ratName, String granularityName, String kpiName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        Area area = areaService.findAreaByName(areaName);

        Long standardKpiId = null;
        if (kpiName != null && !kpiName.isBlank() && !kpiName.equalsIgnoreCase("all")) {
            standardKpiId = standardKpiService.findByKpiName(kpiName, rat).getId();
        }

        LocalDateTime latestDate = kpiAnomalyRepository.getLatestDate(rat.getId(), granularity.getId());
        if (latestDate == null) {
            return new AnomalySummaryDto(List.of(), new AnomalySummaryRowDto("", "Total", 0, 0, 0, 0));
        }
        LocalDateTime currStart = latestDate.toLocalDate().atStartOfDay();
        LocalDateTime currEnd = currStart.plusSeconds(granularity.getPlusSeconds());

        List<AnomalySeverityCountProjection> counts = kpiAnomalyRepository.countAnomaliesByKpiAndSeverity(
                currStart, currEnd, area.getId(), rat.getId(), granularity.getId(), standardKpiId);

        Map<String, AnomalySummaryRowDto> byKpi = new LinkedHashMap<>();
        long grandCritical = 0, grandHigh = 0, grandModerate = 0;

        // Group raw counts per KPI, accumulating critical/high/moderate into one row per KPI
        Map<String, long[]> tally = new LinkedHashMap<>(); // kpiLabel -> [critical, high, moderate]
        Map<String, String> kpiNameByLabel = new LinkedHashMap<>();

        for (AnomalySeverityCountProjection p : counts) {
            long[] arr = tally.computeIfAbsent(p.kpiLabel(), k -> new long[3]);
            kpiNameByLabel.putIfAbsent(p.kpiLabel(), p.kpiName());
            switch (p.severity()) {
                case "critical" -> arr[0] += p.cnt();
                case "high"     -> arr[1] += p.cnt();
                case "moderate" -> arr[2] += p.cnt();
                default -> { }
            }
        }

        List<AnomalySummaryRowDto> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : tally.entrySet()) {
            long[] arr = entry.getValue();
            long total = arr[0] + arr[1] + arr[2];
            rows.add(new AnomalySummaryRowDto(
                    kpiNameByLabel.get(entry.getKey()), entry.getKey(), arr[0], arr[1], arr[2], total));
            grandCritical += arr[0];
            grandHigh += arr[1];
            grandModerate += arr[2];
        }

        // Sort rows by total descending — worst KPIs first
        rows.sort((a, b) -> Long.compare(b.total(), a.total()));

        AnomalySummaryRowDto grandTotal = new AnomalySummaryRowDto(
                "", "Total", grandCritical, grandHigh, grandModerate,
                grandCritical + grandHigh + grandModerate);

        return new AnomalySummaryDto(rows, grandTotal);
    }

    private KpiAnomalyDto toDto(KpiAnomalyProjection p) {
        return new KpiAnomalyDto(
                p.getCellName(), p.getKpiName(), p.getKpiLabel(), p.getUnit(),
                p.getTimestamp(), p.getObservedValue(), p.getBaselineMedian(),
                p.getMad(), p.getRobustZScore(), p.getSeverity()
        );
    }
}
