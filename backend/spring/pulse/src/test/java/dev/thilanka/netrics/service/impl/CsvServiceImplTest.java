package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.CellKpiReportDto;
import dev.thilanka.netrics.dto.SiteKpiReportDto;
import dev.thilanka.netrics.entity.StandardKpi;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Stream;

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

    @Test
    void cellWiseReportStreamsOneRowPerCellAndTimestamp() {
        Timestamp t1 = Timestamp.valueOf("2026-10-01 00:00:00");
        Timestamp t2 = Timestamp.valueOf("2026-10-01 01:00:00");
        Stream<CellKpiReportDto> rows = Stream.of(
                new CellKpiReportDto(t1, "S1", "C1", "3", "a", 1.0),
                new CellKpiReportDto(t1, "S1", "C1", "3", "b", 2.0),
                new CellKpiReportDto(t1, null, "C2", null, "a", 3.0),
                new CellKpiReportDto(t2, "S1", "C1", "3", "b", 4.0));

        StringWriter out = new StringWriter();
        new CsvServiceImpl(null).writeCellWiseReportByKpisAndDateToCsv(rows, List.of(kpi("a", "A"), kpi("b", "B")), out);

        assertEquals(String.join("\r\n",
                "Timestamp,Site ID,Cell,Band,A,B",
                t1 + ",S1,C1,3,1.0,2.0",
                t1 + ",,C2,,3.0,",
                t2 + ",S1,C1,3,,4.0",
                ""), out.toString());
    }
}
