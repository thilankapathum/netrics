package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.CellCsvImportResultDto;
import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.entity.enums.CellCsvHeader;
import dev.thilanka.netrics.entity.enums.CellCsvImportResultHeader;
import dev.thilanka.netrics.service.CsvService;
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

    private static final String[] CELL_HEADERS = Arrays
            .stream(CellCsvHeader.values())
            .map(CellCsvHeader::getHeader)
            .toArray(String[]::new);

    private static final String[] CELL_IMPORT_RESULT_HEADERS = Arrays
            .stream(CellCsvImportResultHeader.values())
            .map(CellCsvImportResultHeader::getHeader)
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
                        dto.bandName()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV: " + e.getMessage());
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
                        record.get(CellCsvHeader.CELL_NAME.getHeader()),
                        record.get(CellCsvHeader.NODE_NAME.getHeader()),
                        record.get(CellCsvHeader.RAT_NAME.getHeader()),
                        record.get(CellCsvHeader.SITE_CODE.getHeader()),
                        record.get(CellCsvHeader.BAND_NAME.getHeader())
                );
                cellDtos.add(dto);
            }
            return cellDtos;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeCellImportResultToCsv(List<CellCsvImportResultDto> results, Writer writer) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(CELL_IMPORT_RESULT_HEADERS)
                .get();

        try (CSVPrinter printer = new CSVPrinter(writer,csvFormat)){
            for (CellCsvImportResultDto result: results){
                printer.printRecord(
                        result.cellDto().cellName(),
                        result.cellDto().siteCode(),
                        result.cellDto().nodeName(),
                        result.cellDto().ratName(),
                        result.cellDto().bandName(),
                        result.status(),
                        result.errorMessage()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Cell Import Result CSV " + e);
        }
    }
}
