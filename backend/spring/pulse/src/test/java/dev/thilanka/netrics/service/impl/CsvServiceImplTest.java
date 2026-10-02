package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.SiteKpiReportDto;
import dev.thilanka.netrics.entity.StandardKpi;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvServiceImplTest {

    private static StandardKpi kpi(String name, String label) {
        StandardKpi k = new StandardKpi();
        k.setKpiName(name);
        k.setLabel(label);
        return k;
    }

    @Test
    void multiKpiReportPivotsKpisIntoColumns() {
        Timestamp d1 = Timestamp.valueOf("2026-10-01 00:00:00");
        List<SiteKpiReportDto> rows = List.of(
                new SiteKpiReportDto(d1, "S1", "a", "A", 1.0, "1+3"),
                new SiteKpiReportDto(d1, "S1", "b", "B", 2.0, "1+3"),
                new SiteKpiReportDto(d1, "S2", "b", "B", 3.0, "3"));

        StringWriter out = new StringWriter();
        new CsvServiceImpl(null).writeSiteWiseReportByKpisAndDateToCsv(rows, List.of(kpi("b", "B"), kpi("a", "A")), out);

        assertEquals(String.join("\r\n",
                "Date,Site ID,B,A,Bands",
                d1 + ",S1,2.0,1.0,1+3",
                d1 + ",S2,3.0,,3",
                ""), out.toString());
    }
}
