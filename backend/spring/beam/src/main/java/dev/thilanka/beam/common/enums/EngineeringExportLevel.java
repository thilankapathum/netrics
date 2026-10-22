package dev.thilanka.beam.common.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Depth of an engineering parameter export. Each level adds its own columns and joins on top of
 * its parents, so a TILT row also carries its antenna, sector and site columns.
 */
public enum EngineeringExportLevel {
    SITE(
            new String[][]{
                    {"site_code", "s.site_code"},
                    {"site_name", "s.site_name"},
                    {"latitude", "s.latitude"},
                    {"longitude", "s.longitude"},
                    {"building_height", "s.building_height"},
                    {"tower_height", "s.tower_height"},
                    {"operator", "o.name"},
                    {"infra_type", "it.infra_type"},
                    {"leg_type", "it.leg_type"}
            },
            """
                    FROM sites s
                    LEFT JOIN operators o ON o.id = s.operator_id
                    LEFT JOIN infra_types it ON it.id = s.infra_type_id
                    """,
            "s.site_code"),
    SECTOR(
            new String[][]{
                    {"sector", "sc.name"},
                    {"sector_index", "sc.sector_index"},
                    {"sector_azimuth", "sc.azimuth"}
            },
            "LEFT JOIN sectors sc ON sc.site_id = s.id\n",
            "sc.sector_index"),
    ANTENNA(
            new String[][]{
                    {"antenna_index", "a.antenna_index"},
                    {"antenna_type", "at.name"},
                    {"manufacturer", "m.name"},
                    {"antenna_azimuth", "a.azimuth"},
                    {"mechanical_tilt", "a.mechanical_tilt"},
                    {"antenna_height", "a.antenna_height"}
            },
            """
                    LEFT JOIN antennas a ON a.sector_id = sc.id
                    LEFT JOIN antenna_types at ON at.id = a.antenna_type_id
                    LEFT JOIN manufacturers m ON m.id = a.manufacturer_id
                    """,
            "a.antenna_index"),
    TILT(
            new String[][]{
                    {"band", "b.name"},
                    {"electrical_tilt", "et.electrical_tilt"}
            },
            """
                    LEFT JOIN electrical_tilts et ON et.antenna_id = a.id
                    LEFT JOIN bands b ON b.id = et.band_id
                    """,
            "b.number");

    private final String[][] columns;
    private final String joins;
    private final String orderBy;

    EngineeringExportLevel(String[][] columns, String joins, String orderBy) {
        this.columns = columns;
        this.joins = joins;
        this.orderBy = orderBy;
    }

    private List<EngineeringExportLevel> levelsUpToThis() {
        return Arrays.asList(values()).subList(0, ordinal() + 1);
    }

    private List<String[]> allColumns() {
        List<String[]> all = new ArrayList<>();
        levelsUpToThis().forEach(level -> all.addAll(Arrays.asList(level.columns)));
        return all;
    }

    public String[] headers() {
        return allColumns().stream().map(c -> c[0]).toArray(String[]::new);
    }

    /** Builds the export query; when {@code singleSite} is true it expects one bind parameter: the site code */
    public String sql(boolean singleSite) {
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(String.join(", ", allColumns().stream().map(c -> c[1]).toList()))
                .append('\n');
        levelsUpToThis().forEach(level -> sql.append(level.joins));
        if (singleSite) {
            sql.append("WHERE s.site_code = ?\n");
        }
        sql.append("ORDER BY ")
                .append(String.join(", ", levelsUpToThis().stream().map(level -> level.orderBy).toList()));
        return sql.toString();
    }
}
