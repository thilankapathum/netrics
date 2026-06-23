package dev.thilanka.beam.common.enums.headers;

import lombok.Getter;

@Getter
public enum SiteKpiReportHeader {
    DATE("Date"),
    SITE_CODE("Site ID"),
//    KPI_NAME("KPI Name"),     DO NOT UNCOMMENT THIS!
    KPI_LABEL("KPI"),
    KPI_VALUE("Value"),
    CONCAT_BANDS("Bands");

    private final String header;
    SiteKpiReportHeader(String header) {this.header = header;}
}