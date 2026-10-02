package dev.thilanka.netrics.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thilanka.netrics.dto.SiteKpiReportDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.enums.ReportJobStatus;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.repository.ReportJobRepository;
import dev.thilanka.netrics.service.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportJobServiceImpl implements ReportJobService {

    private final ReportJobRepository reportJobRepository;
    private final KpiDayRepository kpiDayRepository;
    private final RatService ratService;
    private final GranularityService granularityService;
    private final StandardKpiService standardKpiService;
    private final AreaService areaService;
    private final DateService dateService;
    private final CsvService csvService;
    private final ObjectMapper objectMapper;

    // Self-reference through the Spring proxy so @Async applies (a direct this-call bypasses it)
    @Lazy
    @Autowired
    private ReportJobService self;

    private static final Path REPORTS_DIR = Path.of(System.getProperty("java.io.tmpdir"), "netrics-reports");

    @FunctionalInterface
    private interface ReportWriter {
        /** Writes the report into REPORTS_DIR and returns the file name. */
        String write() throws Exception;
    }

    @Override
    public UUID submitSiteWiseReportJob(String ratName, String granularityName, String standardKpiName, String startDate, String endDate, String areaName, String requestedBy) {
        ReportJob job = createJob("SITE_KPI_DATE", requestedBy, Map.of(
                "ratName", ratName, "granularityName", granularityName,
                "standardKpiName", standardKpiName, "startDate", startDate,
                "endDate", endDate, "areaName", areaName));
        self.generateSiteWiseReportAsync(job.getId(), ratName, granularityName, standardKpiName, startDate, endDate, areaName);
        return job.getId();
    }

    @Override
    @Async("reportTaskExecutor")
    public void generateSiteWiseReportAsync(UUID jobId, String ratName, String granularityName, String standardKpiName, String startDate, String endDate, String areaName) {
        runJob(jobId, () -> {
            Rat rat = ratService.findRatByName(ratName);
            Granularity granularity = granularityService.findGranularityByName(granularityName);
            StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, rat);
            List<SiteKpiReportDto> data = querySiteWiseReport(rat, granularity, List.of(standardKpi), startDate, endDate, areaName);

            String fileName = String.format("%s_%s_%s_%s_%s.csv",
                    sanitize(standardKpiName), sanitize(areaName), sanitize(ratName),
                    sanitize(granularityName), jobId);
            try (Writer writer = Files.newBufferedWriter(REPORTS_DIR.resolve(fileName))) {
                csvService.writeSiteWiseReportByKpiAndDateToCsv(data, writer);
            }
            return fileName;
        });
    }

    @Override
    public UUID submitSiteWiseMultiKpiReportJob(String ratName, String granularityName, List<String> standardKpiNames, String startDate, String endDate, String areaName, String requestedBy) {
        ReportJob job = createJob("SITE_MULTI_KPI_DATE", requestedBy, Map.of(
                "ratName", ratName, "granularityName", granularityName,
                "standardKpiNames", standardKpiNames, "startDate", startDate,
                "endDate", endDate, "areaName", areaName));
        self.generateSiteWiseMultiKpiReportAsync(job.getId(), ratName, granularityName, standardKpiNames, startDate, endDate, areaName);
        return job.getId();
    }

    @Override
    @Async("reportTaskExecutor")
    public void generateSiteWiseMultiKpiReportAsync(UUID jobId, String ratName, String granularityName, List<String> standardKpiNames, String startDate, String endDate, String areaName) {
        runJob(jobId, () -> {
            Rat rat = ratService.findRatByName(ratName);
            Granularity granularity = granularityService.findGranularityByName(granularityName);
            List<StandardKpi> kpis = standardKpiNames.stream()
                    .distinct()
                    .map(name -> standardKpiService.findByKpiName(name, rat))
                    .toList();
            List<SiteKpiReportDto> data = querySiteWiseReport(rat, granularity, kpis, startDate, endDate, areaName);

            String fileName = String.format("MultiKPI_%s_%s_%s_%s.csv",
                    sanitize(areaName), sanitize(ratName), sanitize(granularityName), jobId);
            try (Writer writer = Files.newBufferedWriter(REPORTS_DIR.resolve(fileName))) {
                csvService.writeSiteWiseReportByKpisAndDateToCsv(data, kpis, writer);
            }
            return fileName;
        });
    }

    @Override
    public ReportJob getJob(UUID jobId) {
        return reportJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Report job not found: " + jobId));
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
        List<ReportJob> stale = reportJobRepository.findByStatusAndCreatedAtBefore(ReportJobStatus.COMPLETED, cutoff);
        for (ReportJob job : stale) {
            try {
                if (job.getFilePath() != null) Files.deleteIfExists(Path.of(job.getFilePath()));
            } catch (IOException e) {
                log.warn("Could not delete report file for job {}", job.getId(), e);
            }
            reportJobRepository.delete(job);
        }
    }

    private ReportJob createJob(String reportType, String requestedBy, Map<String, Object> params) {
        ReportJob job = new ReportJob();
        job.setReportType(reportType);
        job.setRequestedBy(requestedBy);
        job.setStatus(ReportJobStatus.PENDING);
        try {
            job.setParamsJson(objectMapper.writeValueAsString(params));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize job params", e);
        }
        return reportJobRepository.save(job);
    }

    private void runJob(UUID jobId, ReportWriter reportWriter) {
        ReportJob job = reportJobRepository.findById(jobId).orElseThrow();
        job.setStatus(ReportJobStatus.PROCESSING);
        reportJobRepository.save(job);

        try {
            Files.createDirectories(REPORTS_DIR);
            String fileName = reportWriter.write();

            job.setFilePath(REPORTS_DIR.resolve(fileName).toString());
            job.setFileName(fileName);
            job.setStatus(ReportJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            reportJobRepository.save(job);

        } catch (Exception e) {
            log.error("Report job {} failed", jobId, e);
            job.setStatus(ReportJobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            reportJobRepository.save(job);
        }
    }

    private List<SiteKpiReportDto> querySiteWiseReport(Rat rat, Granularity granularity, List<StandardKpi> kpis, String startDate, String endDate, String areaName) {
        LocalDateTime startTs = dateService.extractDate(startDate).toLocalDate().atStartOfDay();
        LocalDateTime endTs = dateService.extractDate(endDate).toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
        Area area = areaService.findAreaByName(areaName);
        return kpiDayRepository.getSiteWiseReportByKpiAndDate(
                rat.getId(), granularity.getId(), kpis.stream().map(StandardKpi::getId).toList(), startTs, endTs, area.getId());
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
