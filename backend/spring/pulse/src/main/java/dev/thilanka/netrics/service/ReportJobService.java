package dev.thilanka.netrics.service;

import dev.thilanka.netrics.entity.ReportJob;

import java.util.UUID;

public interface ReportJobService {
    UUID submitSiteWiseReportJob(String ratName, String granularityName, String standardKpiName,
                                 String startDate, String endDate, String areaName, String requestedBy);

    void generateSiteWiseReportAsync(UUID jobId, String ratName, String granularityName,
                                     String standardKpiName, String startDate, String endDate, String areaName);

    ReportJob getJob(UUID jobId);

    void cleanupOldJobs();
}
