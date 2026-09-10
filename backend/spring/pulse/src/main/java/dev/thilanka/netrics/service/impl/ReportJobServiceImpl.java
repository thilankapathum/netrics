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

import static org.apache.kafka.common.utils.Sanitizer.sanitize;

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

    private static final Path REPORTS_DIR = Path.of(System.getProperty("java.io.tmpdir"), "netrics-reports");

    @Override
    public UUID submitSiteWiseReportJob(String ratName, String granularityName, String standardKpiName, String startDate, String endDate, String areaName, String requestedBy) {
        ReportJob job = new ReportJob();
        job.setReportType("SITE_KPI_DATE");
        job.setRequestedBy(requestedBy);
        job.setStatus(ReportJobStatus.PENDING);

        Map<String, String> params = Map.of(
                "ratName", ratName, "granularityName", granularityName,
                "standardKpiName", standardKpiName, "startDate", startDate,
                "endDate", endDate, "areaName", areaName);
        try {
            job.setParamsJson(objectMapper.writeValueAsString(params));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize job params", e);
        }

        job = reportJobRepository.save(job);
        generateSiteWiseReportAsync(job.getId(), ratName, granularityName, standardKpiName, startDate, endDate, areaName);
        return job.getId();
    }

    @Override
    @Async
    public void generateSiteWiseReportAsync(UUID jobId, String ratName, String granularityName, String standardKpiName, String startDate, String endDate, String areaName) {
        ReportJob job = reportJobRepository.findById(jobId).orElseThrow();
        job.setStatus(ReportJobStatus.PROCESSING);
        reportJobRepository.save(job);

        try {
            Files.createDirectories(REPORTS_DIR);

            Rat rat = ratService.findRatByName(ratName);
            Granularity granularity = granularityService.findGranularityByName(granularityName);
            StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, rat);
            LocalDateTime startTs = dateService.extractDate(startDate).toLocalDate().atStartOfDay();
            LocalDateTime endTs = dateService.extractDate(endDate).toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
            Area area = areaService.findAreaByName(areaName);

            List<SiteKpiReportDto> data = kpiDayRepository.getSiteWiseReportByKpiAndDate(
                    rat.getId(), granularity.getId(), standardKpi.getId(), startTs, endTs, area.getId());

            String fileName = String.format("%s_%s_%s_%s_%s.csv",
                    sanitize(standardKpiName), sanitize(areaName), sanitize(ratName),
                    sanitize(granularityName), jobId);
            Path filePath = REPORTS_DIR.resolve(fileName);

            try (Writer writer = Files.newBufferedWriter(filePath)) {
                csvService.writeSiteWiseReportByKpiAndDateToCsv(data, writer);
            }

            job.setFilePath(filePath.toString());
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

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
