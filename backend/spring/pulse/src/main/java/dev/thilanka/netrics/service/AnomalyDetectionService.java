package dev.thilanka.netrics.service;

import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;

public interface AnomalyDetectionService {
//    void runAnomalyDetection();

    void runForRatAndGranularity(Rat rat, Granularity granularity);
}
