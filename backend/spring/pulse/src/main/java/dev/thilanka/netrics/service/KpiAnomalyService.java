package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiAnomalyDto;

import java.util.List;

public interface KpiAnomalyService {
    List<KpiAnomalyDto> getLatestAnomalies(String ratName, String granularityName, String minSeverity);
}
