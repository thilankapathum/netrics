package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.service.AnomalyDetectionService;
import dev.thilanka.netrics.service.GranularityService;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionScheduler {
    private final AnomalyDetectionService anomalyDetectionService;
    private final RatService ratService;
    private final GranularityService granularityService;
    private final KpiDayService kpiDayService;

    private static final List<String> TARGET_GRANULARITIES = List.of("day-average", "busy-hour");

    @Scheduled(cron = "0 0 3 * * *")
    public void runAnomalyDetection() {

        /* Remove duplicate entries for same cell,rat,granularity,timestamp,kpi for different OSS */
        try {
            int deleted = kpiDayService.deduplicateLatestByOss();
            log.info("Deduplication Complete. Deleted {} duplicates", deleted);
        } catch (Exception e) {
            log.error("Deduplication Failed! Error: {}", e.getMessage());
        }

        for (String granName : TARGET_GRANULARITIES) {
            Granularity granularity = granularityService.findGranularityByName(granName);
            for (Rat rat : ratService.findAll()) {
                try {
                    anomalyDetectionService.runForRatAndGranularity(rat, granularity);
                } catch (Exception e) {
                    log.error("Anomaly detection failed: rat={}, granularity={}",
                            rat.getName(), granularity.getName(), e);
                }
            }
        }
    }

    public void runAnomalyDetection(String granularityName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        try {
            anomalyDetectionService.runForRatAndGranularity(rat, granularity);
        } catch (Exception e) {
            log.error("Anomaly detection failed: rat={}, granularity={}",
                    rat.getName(), granularity.getName(), e);
        }
    }
}
