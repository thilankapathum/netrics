package dev.thilanka.beam.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineeringExportLevelTest {

    @Test
    void headersMatchSelectedColumnsAtEveryLevel() {
        for (EngineeringExportLevel level : EngineeringExportLevel.values()) {
            String select = level.sql(false).split("\n")[0];
            assertEquals(level.headers().length, select.split(",").length, level.name());
        }
    }

    @Test
    void joinsStopAtTheChosenLevel() {
        assertFalse(EngineeringExportLevel.SITE.sql(false).contains("sectors"));
        assertFalse(EngineeringExportLevel.SECTOR.sql(false).contains("antennas"));
        assertFalse(EngineeringExportLevel.ANTENNA.sql(false).contains("electrical_tilts"));
        assertTrue(EngineeringExportLevel.TILT.sql(false).contains("electrical_tilts"));
    }

    @Test
    void siteFilterOnlyWhenRequested() {
        assertFalse(EngineeringExportLevel.TILT.sql(false).contains("WHERE"));
        assertTrue(EngineeringExportLevel.TILT.sql(true).contains("WHERE s.site_code = ?"));
    }
}
