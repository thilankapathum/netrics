package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;

public interface CsvService {

    void writeCellsToCsv(List<CellDto> dtos, Writer writer);

    void writeSitesToCsv(List<SiteDto> dtos, Writer writer);

    List<CellDto> readCellsFromCsv(InputStream inputStream);

    List<SiteDto> readSitesFromCsv(InputStream inputStream);

    void writeCellImportResultToCsv(List<CellCsvImportResultDto> results, Writer writer);

    void writeSiteImportResultToCsv(List<SiteCsvImportResultDto> results, Writer writer);

    void writeSiteWiseReportByKpiAndDateToCsv(List<SiteKpiReportDto> dtos, Writer writer) throws IOException;
}
