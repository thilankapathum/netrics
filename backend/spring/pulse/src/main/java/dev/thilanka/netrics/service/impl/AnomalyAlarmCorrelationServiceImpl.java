package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.repository.AnomalyAlarmCorrelationRepository;
import dev.thilanka.netrics.service.AnomalyAlarmCorrelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyAlarmCorrelationServiceImpl implements AnomalyAlarmCorrelationService {
    private final AnomalyAlarmCorrelationRepository anomalyAlarmCorrelationRepository;

    @Value("${netrics.alarm-correlation.lookback-days:2}")
    private int lookbackDays;

    @Override
    public void runCorrelation() {
        log.info("Starting alarm correlation run, lookbackDays={}", lookbackDays);

        int inserted = anomalyAlarmCorrelationRepository.insertCorrelations(lookbackDays);
        log.info("Inserted {} new anomaly-alarm correlation rows", inserted);

        int rolledUp = anomalyAlarmCorrelationRepository.refreshRollups(lookbackDays);
        log.info("Refreshed rollup columns on {} anomalies", rolledUp);
    }
}
