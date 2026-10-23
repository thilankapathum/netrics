package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.StandardKpi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.stream.Stream;

public interface CsvService {

    void writeCellsToCsv(List<CellDto> dtos, Writer writer);

    void writeSitesToCsv(List<SiteDto> dtos, Writer writer);

    void writeSectorsToCsv(List<SectorDto> dtos, Writer writer);

    List<CellDto> readCellsFromCsv(InputStream inputStream);

    List<SiteDto> readSitesFromCsv(InputStream inputStream);

    List<SectorDto> readSectorsFromCsv(InputStream inputStream);

    List<CellMappingDto> readCellMappingsFromCsv(InputStream inputStream);

    void writeCellImportResultToCsv(List<CellCsvImportResultDto> results, Writer writer);

    void writeCellMappingImportResultToCsv(List<CellMappingCsvImportResultDto> results, Writer writer);

    void writeSiteImportResultToCsv(List<SiteCsvImportResultDto> results, Writer writer);

    void writeSectorImportResultToCsv(List<SectorCsvImportResultDto> results, Writer writer);

    void writeSiteWiseReportByKpiAndDateToCsv(List<SiteKpiReportDto> dtos, Writer writer) throws IOException;

    void writeSiteWiseReportByKpisAndDateToCsv(List<SiteKpiReportDto> dtos, List<StandardKpi> kpis, Writer writer);

    void writeCellWiseReportByKpisAndDateToCsv(Stream<CellKpiReportDto> rows, List<StandardKpi> kpis, Writer writer);
}
