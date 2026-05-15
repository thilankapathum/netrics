package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.SectorCsvImportResultDto;
import dev.thilanka.beam.dto.SectorDto;
import dev.thilanka.beam.dto.SiteCsvImportResultDto;
import dev.thilanka.beam.dto.SiteDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;

public interface CsvService {

//    void writeCellsToCsv(List<CellDto> dtos, Writer writer);

    void writeSitesToCsv(List<SiteDto> dtos, Writer writer);

    void writeSectorsToCsv(List<SectorDto> dtos, Writer writer);

//    List<CellDto> readCellsFromCsv(InputStream inputStream);

    List<SiteDto> readSitesFromCsv(InputStream inputStream);

    List<SectorDto> readSectorsFromCsv(InputStream inputStream);

//    void writeCellImportResultToCsv(List<CellCsvImportResultDto> results, Writer writer);

    void writeSiteImportResultToCsv(List<SiteCsvImportResultDto> results, Writer writer);

    void writeSectorImportResultToCsv(List<SectorCsvImportResultDto> results, Writer writer);

//    void writeSiteWiseReportByKpiAndDateToCsv(List<SiteKpiReportDto> dtos, Writer writer) throws IOException;
}
