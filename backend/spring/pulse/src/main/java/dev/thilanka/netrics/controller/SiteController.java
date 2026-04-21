package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CellCsvImportResultDto;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.SiteCsvImportResultDto;
import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.service.CsvService;
import dev.thilanka.netrics.service.SiteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/sites")
@RequiredArgsConstructor
public class SiteController {
    private final SiteService siteService;
    private final CsvService csvService;


    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    public ResponseEntity<SiteDto> createSite(@RequestBody @Valid SiteDto dto){
        SiteDto savedDto = siteService.createSite(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<SiteDto>> createSiteList(@RequestBody @Valid List<SiteDto> dtos){
        List<SiteDto> savedDtos = siteService.createSites(dtos);
        return new ResponseEntity<>(savedDtos,HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<SiteDto> getSiteBySiteCode(@RequestParam("siteCode") String siteCode){
        SiteDto dto = siteService.getBySiteCode(siteCode);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("missing/export")
    public void exportSitesWithMissingInfo(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sites_missing_info.csv");

        List<SiteDto> siteDtos = siteService.getSitesWithMissingInfo();
        csvService.writeSitesToCsv(siteDtos, response.getWriter());
        siteService.reloadSiteCountWithMissingInfo();
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("all/export")
    public void exportAllCells(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_sites_info.csv");

        List<SiteDto> siteDtos = siteService.getAllSites();

        csvService.writeSitesToCsv(siteDtos, response.getWriter());
        siteService.reloadSiteCountWithMissingInfo();
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping(value = "missing/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity importSitesWithCorrectedInfo(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        List<SiteDto> importedSiteDtos = csvService.readSitesFromCsv(file.getInputStream());
        List<SiteCsvImportResultDto> results = siteService.updateSitesWithResults(importedSiteDtos);
//        cellService.reloadCellCountWithMissingInfo();

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = site_import_result.csv");
        csvService.writeSiteImportResultToCsv(results, response.getWriter());
        siteService.reloadSiteCountWithMissingInfo();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("missing/count")
    public ResponseEntity<Integer> getSiteCountWithMissingInfo(){
        Integer count = siteService.getSiteCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("missing/reload-count")
    public ResponseEntity<Integer> reloadSiteCountWithMissingInfo(){
        Integer count = siteService.reloadSiteCountWithMissingInfo();
        return ResponseEntity.ok(count);
    }

}
