package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.SiteKpiReportDto;
import dev.thilanka.netrics.service.CsvService;
import dev.thilanka.netrics.service.KpiReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/kpi-reports")
@RequiredArgsConstructor
public class KpiReportController {
    private final KpiReportService kpiReportService;
    private final CsvService csvService;

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
}
