package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.SiteKpiReportDto;
import java.util.List;

public interface KpiReportService {
    List<SiteKpiReportDto> getSiteWiseReportByKpiAndDate(String ratName, String granularityName, String standardKpiName, String startDate, String endDate, String areaName);
}
