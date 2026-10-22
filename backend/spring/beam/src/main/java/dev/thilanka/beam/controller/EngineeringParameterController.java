package dev.thilanka.beam.controller;

import dev.thilanka.beam.common.enums.EngineeringExportLevel;
import dev.thilanka.beam.service.EngineeringParameterExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/beam/engineering-parameters")
@RequiredArgsConstructor
public class EngineeringParameterController {
    private final EngineeringParameterExportService exportService;

    @PreAuthorize("hasAuthority('ROLE_BEAM_READ')")
    @GetMapping("export")
    public void export(@RequestParam("level") EngineeringExportLevel level,
                       @RequestParam(value = "siteCode", required = false) String siteCode,
                       HttpServletResponse response) throws IOException {
        String scope = siteCode == null || siteCode.isBlank() ? "" : "_" + siteCode.replaceAll("[^A-Za-z0-9_-]", "");
        String fileName = "engineering_parameters_" + level.name().toLowerCase() + scope + "_"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        exportService.export(level, siteCode, response.getWriter());
    }
}
