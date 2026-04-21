package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.BusinessValidationException;
import dev.thilanka.netrics.common.exception.FileProcessingException;
import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.enums.headers.*;
import dev.thilanka.netrics.service.CsvService;
import dev.thilanka.netrics.util.DataTypeUtilService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {

    private final DataTypeUtilService dataTypeUtilService;

    private static final String[] CELL_HEADERS = Arrays
            .stream(CellCsvHeader.values())
            .map(CellCsvHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] CELL_IMPORT_RESULT_HEADERS = Arrays
            .stream(CellCsvImportResultHeader.values())
            .map(CellCsvImportResultHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] SITE_HEADERS = Arrays
            .stream(SiteCsvHeader.values())
            .map(SiteCsvHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] SITE_IMPORT_RESULT_HEADERS = Arrays
            .stream(SiteCsvImportResultHeader.values())
            .map(SiteCsvImportResultHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] SECTOR_HEADERS = Arrays
            .stream(SectorCsvHeader.values())
            .map(SectorCsvHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] SECTOR_IMPORT_RESULT_HEADERS = Arrays
            .stream(SectorCsvImportResultHeader.values())
            .map(SectorCsvImportResultHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] SITE_KPI_REPORT_HEADERS = Arrays
            .stream(SiteKpiReportHeader.values())
            .map(SiteKpiReportHeader::getHeader)
            .toArray(String[]::new);

    @Override
    public void writeCellsToCsv(List<CellDto> dtos, Writer writer) {

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(CELL_HEADERS)
                .get();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            for (CellDto dto : dtos) {
                csvPrinter.printRecord(
                        dto.cellName(),
                        dto.siteCode(),
                        dto.nodeName(),
                        dto.ratName(),
                        dto.bandName(),
                        dto.azimuth(),
                        dto.beamwidth(),
                        dto.isMultiBeam(),
                        dto.carrierName(),
                        dto.sectorName()
                );
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }

    @Override
    public void writeSitesToCsv(List<SiteDto> dtos, Writer writer) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SITE_HEADERS)
                .get();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            for (SiteDto dto : dtos) {
                csvPrinter.printRecord(
                        dto.siteCode(),
                        dto.siteName(),
                        dto.latitude(),
                        dto.longitude()
                );
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }

    @Override
    public void writeSectorsToCsv(List<SectorDto> dtos, Writer writer) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SECTOR_HEADERS)
                .get();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            for (SectorDto dto : dtos) {
                csvPrinter.printRecord(
                        dto.siteCode(),
                        dto.name(),
                        dto.sectorIndex(),
                        dto.azimuth()
                );
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }

    @Override
    public List<CellDto> readCellsFromCsv(InputStream inputStream) {

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = CSVParser.parse(reader, csvFormat)) {

            List<CellDto> cellDtos = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                CellDto dto = new CellDto(
                        dataTypeUtilService.normalizeString((record.get(CellCsvHeader.CELL_NAME.getHeader()))),
                        dataTypeUtilService.normalizeString(record.get(CellCsvHeader.NODE_NAME.getHeader())),
                        dataTypeUtilService.normalizeString(record.get(CellCsvHeader.RAT_NAME.getHeader())),
                        dataTypeUtilService.normalizeString(record.get(CellCsvHeader.SITE_CODE.getHeader())),
                        dataTypeUtilService.normalizeString(record.get(CellCsvHeader.BAND_NAME.getHeader())),
                        dataTypeUtilService.parseInteger(dataTypeUtilService.normalizeString(record.get(CellCsvHeader.AZIMUTH.getHeader()))),
                        dataTypeUtilService.parseInteger(dataTypeUtilService.normalizeString(record.get(CellCsvHeader.BEAMWIDTH.getHeader()))),
                        dataTypeUtilService.parseBoolean(dataTypeUtilService.normalizeString(record.get(CellCsvHeader.IS_MULTI_BEAM.getHeader()))),
                        dataTypeUtilService.normalizeString(record.get(CellCsvHeader.CARRIER_NAME.getHeader())),
                        dataTypeUtilService.normalizeString(record.get(CellCsvHeader.SECTOR_NAME.getHeader()))
                );
                cellDtos.add(dto);
            }
            return cellDtos;

        } catch (IOException e) {
            throw new FileProcessingException("Failed to read CSV", e);
        }
    }

    @Override
    public List<SiteDto> readSitesFromCsv(InputStream inputStream) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = CSVParser.parse(reader, csvFormat)) {

            List<SiteDto> siteDtos = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                SiteDto dto = new SiteDto(
                        record.get(SiteCsvHeader.SITE_CODE.getHeader()),
                        record.get(SiteCsvHeader.SITE_NAME.getHeader()),
                        dataTypeUtilService.parseDouble(record.get(SiteCsvHeader.LATITUDE.getHeader())),
                        dataTypeUtilService.parseDouble(record.get(SiteCsvHeader.LONGITUDE.getHeader()))
                );
                siteDtos.add(dto);
            }
            return siteDtos;

        } catch (IOException e) {
            throw new FileProcessingException("Failed to read CSV", e);
        }
    }

    @Override
    public List<SectorDto> readSectorsFromCsv(InputStream inputStream) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = CSVParser.parse(reader, csvFormat)) {
            List<SectorDto> sectorDtos = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                SectorDto dto = new SectorDto(
                        dataTypeUtilService.parseInteger(record.get(SectorCsvHeader.SECTOR_INDEX.getHeader())),
                        record.get(SectorCsvHeader.SECTOR_NAME.getHeader()),
                        dataTypeUtilService.parseInteger(record.get(SectorCsvHeader.AZIMUTH.getHeader())),
                        record.get(SectorCsvHeader.SITE_CODE.getHeader())
                );
                sectorDtos.add(dto);
            }
            return sectorDtos;
        } catch (IOException e) {
            throw new FileProcessingException("Failed to read CSV", e);
        }
    }

    @Override
    public void writeCellImportResultToCsv(List<CellCsvImportResultDto> results, Writer writer) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(CELL_IMPORT_RESULT_HEADERS)
                .get();

        try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
            for (CellCsvImportResultDto result : results) {
                printer.printRecord(
                        result.cellDto().cellName(),
                        result.cellDto().siteCode(),
                        result.cellDto().nodeName(),
                        result.cellDto().ratName(),
                        result.cellDto().bandName(),
                        result.cellDto().azimuth(),
                        result.cellDto().beamwidth(),
                        result.cellDto().isMultiBeam(),
                        result.cellDto().carrierName(),
                        result.cellDto().sectorName(),
                        result.status(),
                        result.errorMessage()
                );
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }

    @Override
    public void writeSiteImportResultToCsv(List<SiteCsvImportResultDto> results, Writer writer) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SITE_IMPORT_RESULT_HEADERS)
                .get();

        try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
            for (SiteCsvImportResultDto result : results) {
                printer.printRecord(
                        result.siteDto().siteCode(),
                        result.siteDto().siteName(),
                        result.siteDto().latitude(),
                        result.siteDto().longitude(),
                        result.status(),
                        result.errorMessage()
                );
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }

    @Override
    public void writeSectorImportResultToCsv(List<SectorCsvImportResultDto> results, Writer writer) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SECTOR_IMPORT_RESULT_HEADERS)
                .get();

        try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
            for (SectorCsvImportResultDto result : results) {
                printer.printRecord(
                        result.sectorDto().siteCode(),
                        result.sectorDto().name(),
                        result.sectorDto().sectorIndex(),
                        result.sectorDto().azimuth(),
                        result.status(),
                        result.errorMessage()
                );
            }
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }

    @Override
    public void writeSiteWiseReportByKpiAndDateToCsv(List<SiteKpiReportDto> dtos, Writer writer) {

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SITE_KPI_REPORT_HEADERS)
                .get();

        try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
            for (SiteKpiReportDto dto : dtos) {
                printer.printRecord(
                        dto.timestamp(),
                        dto.siteCode(),
                        dto.label(),
                        dto.kpiValue(),
                        dto.concatBands()
                );
            }

        } catch (IOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }

    }
}
