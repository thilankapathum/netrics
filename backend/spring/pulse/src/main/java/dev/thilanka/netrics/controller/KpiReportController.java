package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.ReportJobStatusDto;
import dev.thilanka.netrics.dto.SiteKpiReportDto;
import dev.thilanka.netrics.entity.ReportJob;
import dev.thilanka.netrics.entity.enums.ReportJobStatus;
import dev.thilanka.netrics.service.CsvService;
import dev.thilanka.netrics.service.KpiReportService;
import dev.thilanka.netrics.service.ReportJobService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pulse/kpi-reports")
@RequiredArgsConstructor
public class KpiReportController {
    private final KpiReportService kpiReportService;
    private final CsvService csvService;
    private final ReportJobService reportJobService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("export/site-kpi-date")
    void exportSiteWiseReportByKpiAndDate(
            HttpServletResponse response,
            @RequestParam("ratName") String ratName,
            @RequestParam("granularityName") String granularityName,
            @RequestParam("standardKpiName") String standardKpiName,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("areaName") String areaName) throws IOException {
        response.setContentType("text/csv");

        String filename = String.format("%s_%s_%s_%s.csv",
                sanitizeFilename(standardKpiName),
                sanitizeFilename(areaName),
                sanitizeFilename(ratName),
                sanitizeFilename(granularityName));

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        List<SiteKpiReportDto> kpiReportDtos = kpiReportService.getSiteWiseReportByKpiAndDate(ratName, granularityName, standardKpiName, startDate, endDate, areaName);
        csvService.writeSiteWiseReportByKpiAndDateToCsv(kpiReportDtos, response.getWriter());
    }

    private String sanitizeFilename(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping("export/site-kpi-date/jobs")
    public ResponseEntity<ReportJobStatusDto> submitSiteWiseReportJob(
            @RequestParam String ratName,
            @RequestParam String granularityName,
            @RequestParam String standardKpiName,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String areaName,
            Authentication authentication) {

        UUID jobId = reportJobService.submitSiteWiseReportJob(
                ratName, granularityName, standardKpiName, startDate, endDate, areaName,
                authentication != null ? authentication.getName() : "ANONYMOUS");

        return ResponseEntity.accepted()
                .body(new ReportJobStatusDto(jobId, ReportJobStatus.PENDING, null, null));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("jobs/{jobId}")
    public ResponseEntity<ReportJobStatusDto> getJobStatus(@PathVariable UUID jobId) {
        ReportJob job = reportJobService.getJob(jobId);
        return ResponseEntity.ok(new ReportJobStatusDto(
                job.getId(), job.getStatus(), job.getErrorMessage(), job.getFileName()));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("jobs/{jobId}/download")
    public void downloadJobResult(@PathVariable UUID jobId, HttpServletResponse response) throws IOException {
        ReportJob job = reportJobService.getJob(jobId);

        if (job.getStatus() != ReportJobStatus.COMPLETED || job.getFilePath() == null) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Report not ready");
            return;
        }

        Path filePath = Path.of(job.getFilePath());
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_GONE, "Report file no longer available");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.getFileName() + "\"");
        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }
}
