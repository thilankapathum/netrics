package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.enums.EngineeringExportLevel;
import dev.thilanka.beam.common.exception.FileProcessingException;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.repository.SiteRepository;
import dev.thilanka.beam.service.EngineeringParameterExportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

@Service
@RequiredArgsConstructor
public class EngineeringParameterExportServiceImpl implements EngineeringParameterExportService {

    private final JdbcTemplate jdbcTemplate;
    private final SiteRepository siteRepository;

    @Override
    public void export(EngineeringExportLevel level, String siteCode, Writer writer) {
        boolean singleSite = siteCode != null && !siteCode.isBlank();
        if (singleSite && siteRepository.findBySiteCode(siteCode).isEmpty()) {
            throw new ResourceNotFoundException("Site", "siteCode", siteCode);
        }

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(level.headers())
                .get();
        int columnCount = level.headers().length;
        Object[] args = singleSite ? new Object[]{siteCode} : new Object[0];

        // rows go straight from the result set to the response, so a full-network export never sits in memory
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            jdbcTemplate.query(level.sql(singleSite), (RowCallbackHandler) rs -> {
                try {
                    for (int i = 1; i <= columnCount; i++) {
                        csvPrinter.print(rs.getObject(i));
                    }
                    csvPrinter.println();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, args);
        } catch (IOException | UncheckedIOException e) {
            throw new FileProcessingException("Failed to write CSV", e);
        }
    }
}
