package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.DataTypeUtils;
import dev.thilanka.beam.common.enums.headers.*;
import dev.thilanka.beam.common.exception.FileProcessingException;
import dev.thilanka.beam.dto.SectorCsvImportResultDto;
import dev.thilanka.beam.dto.SectorDto;
import dev.thilanka.beam.dto.SiteCsvImportResultDto;
import dev.thilanka.beam.dto.SiteDto;
import dev.thilanka.beam.service.CsvService;
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

    private final DataTypeUtils dataTypeUtils;

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
                        dto.longitude(),
                        dto.buildingHeight(),
                        dto.towerHeight(),
                        dto.operatorName(),
                        dto.infraType()
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
                        dataTypeUtils.parseDouble(record.get(SiteCsvHeader.LATITUDE.getHeader())),
                        dataTypeUtils.parseDouble(record.get(SiteCsvHeader.LONGITUDE.getHeader())),
                        dataTypeUtils.parseShort(record.get(SiteCsvHeader.BUILDING_HEIGHT.getHeader())),
                        dataTypeUtils.parseShort(record.get(SiteCsvHeader.TOWER_HEIGHT.getHeader())),
                        record.get(SiteCsvHeader.OPERATOR_NAME.getHeader()),
                        record.get(SiteCsvHeader.INFRA_TYPE_NAME.getHeader())

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
                        dataTypeUtils.parseInteger(record.get(SectorCsvHeader.SECTOR_INDEX.getHeader())),
                        record.get(SectorCsvHeader.SECTOR_NAME.getHeader()),
                        dataTypeUtils.parseInteger(record.get(SectorCsvHeader.AZIMUTH.getHeader())),
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
                        result.siteDto().buildingHeight(),
                        result.siteDto().towerHeight(),
                        result.siteDto().operatorName(),
                        result.siteDto().infraType(),
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
}
