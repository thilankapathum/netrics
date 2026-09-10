package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AnomalySummaryDto;
import dev.thilanka.netrics.dto.KpiAnomalyDto;
import dev.thilanka.netrics.dto.PagedResponse;
import dev.thilanka.netrics.dto.WorstCellsDto;

import java.util.List;

public interface KpiAnomalyService {
    List<KpiAnomalyDto> getLatestAnomalies(String ratName, String granularityName, String minSeverity);

    List<WorstCellsDto> getAllAnomalyCellsByArea(String period, String areaName, String ratName, String granularityName);

    PagedResponse<WorstCellsDto> getAllAnomalyCellsByArea(
            String period, String areaName, String ratName, String granularityName,
            String severity, String kpiName, String sortBy, String sortDir, int page, int pageSize, String alarmCorrelation);

    AnomalySummaryDto getAnomalySummaryByArea(String areaName, String ratName, String granularityName, String kpiName);
}
