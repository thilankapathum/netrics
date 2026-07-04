package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BreachingCellProjection;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.KpiAnomalyRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.AnomalyDetectionService;
import dev.thilanka.netrics.service.GranularityService;
import dev.thilanka.netrics.service.RatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private final KpiAnomalyRepository kpiAnomalyRepository;
    private final KpiDayRepository kpiDayRepository;

    private static final int BASELINE_DAYS = 21;
    private static final int MIN_HISTORY = 10;
    private static final double Z_THRESHOLD = 3.5;
    private static final int BATCH_SIZE = 10_000;

    @Override
    @Transactional
    public void runForRatAndGranularity(Rat rat, Granularity granularity) {
        log.info("Running anomaly detection for {} - {}", granularity.getLabel(), rat.getLabel());
        kpiAnomalyRepository.setWorkMem64();

        LocalDateTime latestDate = kpiDayRepository.getLatestDate(rat.getId(), granularity.getId());
        if (latestDate == null) {
            log.warn("No data found: rat={}, granularity={}", rat.getName(), granularity.getName());
            return;
        }

        LocalDateTime currStart = latestDate.toLocalDate().atStartOfDay();
        LocalDateTime currEnd = currStart.plusSeconds(granularity.getPlusSeconds());

        log.info("Start date: {} | End date: {}", currStart, currEnd);
        log.info("Generating breaching cells for {} - {}",  granularity.getLabel(), rat.getLabel());
        List<BreachingCellProjection> breaches =
                kpiAnomalyRepository.findBreachingCells(currStart, currEnd, rat.getId(), granularity.getId());

        if (breaches.isEmpty()) {
            log.info("No breaching cells: rat={}, granularity={}, date={}",
                    rat.getName(), granularity.getName(), currStart.toLocalDate());
            return;
        }

        int totalInserted = 0;
        log.info("Inserting into database init...");
        for (int i = 0; i < breaches.size(); i += BATCH_SIZE) {
            List<BreachingCellProjection> batch =
                    breaches.subList(i, Math.min(i + BATCH_SIZE, breaches.size()));

            log.info("Inserting {} breaching cells", batch.size());

            String[] cellNames = batch.stream().map(BreachingCellProjection::getCellName).toArray(String[]::new);
            Long[] kpiIds = batch.stream().map(BreachingCellProjection::getStandardKpiId).toArray(Long[]::new);

            log.info("Inserting {} KPIs into DB...", cellNames.length);

            totalInserted += kpiAnomalyRepository.detectAndInsertAnomalies(
                    cellNames, kpiIds, rat.getId(), granularity.getId(), currStart, currEnd,
                    BASELINE_DAYS, MIN_HISTORY, Z_THRESHOLD);

            log.info("Inserted {} cells", totalInserted);
        }

        log.info("Anomaly detection complete: rat={}, granularity={}, date={}, breaching={}, flagged={}",
                rat.getName(), granularity.getName(), currStart.toLocalDate(), breaches.size(), totalInserted);
    }
}
