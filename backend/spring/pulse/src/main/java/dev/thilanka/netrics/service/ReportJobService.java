package dev.thilanka.netrics.service;

import dev.thilanka.netrics.entity.ReportJob;

import java.util.List;
import java.util.UUID;

public interface ReportJobService {
    UUID submitSiteWiseReportJob(String ratName, String granularityName, String standardKpiName,
                                 String startDate, String endDate, String areaName, String requestedBy);

    void generateSiteWiseReportAsync(UUID jobId, String ratName, String granularityName,
                                     String standardKpiName, String startDate, String endDate, String areaName);

    UUID submitSiteWiseMultiKpiReportJob(String ratName, String granularityName, List<String> standardKpiNames,
                                         String startDate, String endDate, String areaName, String requestedBy);

    void generateSiteWiseMultiKpiReportAsync(UUID jobId, String ratName, String granularityName,
                                             List<String> standardKpiNames, String startDate, String endDate, String areaName);

    UUID submitCellWiseReportJob(String ratName, String granularityName, List<String> standardKpiNames,
                                 String startDate, String endDate, String areaName, String requestedBy);

    void generateCellWiseReportAsync(UUID jobId, String ratName, String granularityName,
                                     List<String> standardKpiNames, String startDate, String endDate, String areaName);

    ReportJob getJob(UUID jobId);

    void cleanupOldJobs();
}
