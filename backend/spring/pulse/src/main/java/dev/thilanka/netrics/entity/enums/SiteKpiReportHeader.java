package dev.thilanka.netrics.entity.enums;

import lombok.Getter;

import java.time.LocalDateTime;

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