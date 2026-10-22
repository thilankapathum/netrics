package dev.thilanka.beam.service;

import dev.thilanka.beam.common.enums.EngineeringExportLevel;

import java.io.Writer;

public interface EngineeringParameterExportService {

    /** Streams the engineering parameters down to {@code level} as CSV; {@code siteCode} null exports all sites */
    void export(EngineeringExportLevel level, String siteCode, Writer writer);
}
