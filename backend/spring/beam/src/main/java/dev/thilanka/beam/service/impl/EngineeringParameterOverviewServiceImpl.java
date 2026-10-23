package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.PulseClient;
import dev.thilanka.beam.dto.EngineeringOverviewDto;
import dev.thilanka.beam.dto.EngineeringOverviewDto.Breakdowns;
import dev.thilanka.beam.dto.EngineeringOverviewDto.Counts;
import dev.thilanka.beam.dto.EngineeringOverviewDto.Gap;
import dev.thilanka.beam.dto.EngineeringOverviewDto.NameCount;
import dev.thilanka.beam.dto.OperationLogDto;
import dev.thilanka.beam.service.EngineeringParameterOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineeringParameterOverviewServiceImpl implements EngineeringParameterOverviewService {

    static final int GAP_SAMPLE = 20;
    static final int MAX_PAGE_SIZE = 50;

    /** Every overview query starts with this, so it only sees the sites in the selected area */
    private static final String AREA_SITES = "WITH area_sites AS (SELECT * FROM sites WHERE site_code = ANY(?)) ";

    /** Each gap query selects one {@code site_code} per offending row */
    private record GapQuery(String key, String label, String sql) {
    }

    private static final List<GapQuery> GAPS = List.of(
            new GapQuery("sitesWithoutSectors", "Sites without sectors",
                    "SELECT si.site_code FROM area_sites si WHERE NOT EXISTS (SELECT 1 FROM sectors s WHERE s.site_id = si.id)"),
            new GapQuery("sectorsWithoutAntennas", "Sectors without antennas",
                    "SELECT si.site_code FROM sectors s JOIN area_sites si ON si.id = s.site_id "
                            + "WHERE NOT EXISTS (SELECT 1 FROM antennas a WHERE a.sector_id = s.id)"),
            new GapQuery("antennasWithoutTilts", "Antennas without electrical tilts",
                    "SELECT si.site_code FROM antennas a JOIN sectors s ON s.id = a.sector_id JOIN area_sites si ON si.id = s.site_id "
                            + "WHERE NOT EXISTS (SELECT 1 FROM electrical_tilts et WHERE et.antenna_id = a.id)"),
            new GapQuery("sectorAzimuthMissing", "Sectors with no azimuth",
                    "SELECT si.site_code FROM sectors s JOIN area_sites si ON si.id = s.site_id WHERE s.azimuth IS NULL"),
            new GapQuery("towerHeightMissing", "Sites with no tower height",
                    "SELECT si.site_code FROM area_sites si WHERE si.tower_height IS NULL")
    );

    /*
     * The changed row's site/sector/antenna come from the live tables, or from the parent id in the logged
     * snapshot when the row itself has been deleted. Ids are compared as text because entity_id is VARCHAR.
     */
    private static final String CHANGES_SQL = """
            SELECT l.id, l.entity_name, l.entity_id, l.operation, l.performed_at, l.performed_by,
                   l.changes::text AS changes, si.site_code, s.name AS sector_name, a.antenna_index
            FROM (SELECT * FROM operation_log
                  WHERE entity_name IN ('Site', 'Sector', 'Antenna', 'ElectricalTilt')
                  ORDER BY performed_at DESC, id DESC LIMIT ? OFFSET ?) l
            LEFT JOIN electrical_tilts et ON l.entity_name = 'ElectricalTilt' AND et.id::text = l.entity_id
            LEFT JOIN antennas a ON a.id::text = CASE l.entity_name
                    WHEN 'Antenna' THEN l.entity_id
                    WHEN 'ElectricalTilt' THEN COALESCE(et.antenna_id::text,
                            l.changes -> 'antenna' ->> 'new', l.changes -> 'antenna' ->> 'old') END
            LEFT JOIN sectors s ON s.id::text = CASE l.entity_name
                    WHEN 'Sector' THEN l.entity_id
                    WHEN 'Antenna' THEN COALESCE(a.sector_id::text,
                            l.changes -> 'sector' ->> 'new', l.changes -> 'sector' ->> 'old')
                    WHEN 'ElectricalTilt' THEN a.sector_id::text END
            LEFT JOIN sites si ON si.id::text = CASE l.entity_name
                    WHEN 'Site' THEN l.entity_id
                    WHEN 'Sector' THEN COALESCE(s.site_id::text,
                            l.changes -> 'site' ->> 'new', l.changes -> 'site' ->> 'old')
                    ELSE s.site_id::text END
            ORDER BY l.performed_at DESC, l.id DESC
            """;

    private final JdbcTemplate jdbcTemplate;
    private final PulseClient pulseClient;

    // ponytail: no cache, these are aggregates over a few thousand rows; add @Cacheable if it shows in slow logs
    @Override
    public EngineeringOverviewDto overview(String areaName) {
        // area membership lives in pulse (area -> district codes -> cells -> sites)
        String[] siteCodes = pulseClient.areaSiteCodes(areaName).toArray(String[]::new);

        Counts counts = jdbcTemplate.queryForObject(AREA_SITES + """
                SELECT (SELECT count(*) FROM area_sites),
                       (SELECT count(*) FROM sectors s JOIN area_sites si ON si.id = s.site_id),
                       (SELECT count(*) FROM antennas a JOIN sectors s ON s.id = a.sector_id
                                                        JOIN area_sites si ON si.id = s.site_id),
                       (SELECT count(*) FROM electrical_tilts et JOIN antennas a ON a.id = et.antenna_id
                                                                 JOIN sectors s ON s.id = a.sector_id
                                                                 JOIN area_sites si ON si.id = s.site_id)
                """, (rs, i) -> new Counts(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)), (Object) siteCodes);

        Breakdowns breakdowns = new Breakdowns(
                groups(siteCodes, "SELECT o.name AS name FROM area_sites si LEFT JOIN operators o ON o.id = si.operator_id"),
                groups(siteCodes, "SELECT it.infra_type AS name FROM area_sites si LEFT JOIN infra_types it ON it.id = si.infra_type_id"),
                groups(siteCodes, "SELECT b.name AS name FROM electrical_tilts et LEFT JOIN bands b ON b.id = et.band_id "
                        + "JOIN antennas a ON a.id = et.antenna_id JOIN sectors s ON s.id = a.sector_id JOIN area_sites si ON si.id = s.site_id"),
                groups(siteCodes, "SELECT m.name AS name FROM antennas a LEFT JOIN manufacturers m ON m.id = a.manufacturer_id "
                        + "JOIN sectors s ON s.id = a.sector_id JOIN area_sites si ON si.id = s.site_id"));

        List<Gap> gaps = GAPS.stream().map(q -> gap(siteCodes, q)).toList();
        return new EngineeringOverviewDto(counts, breakdowns, gaps);
    }

    @Override
    public List<OperationLogDto> changes(int page, int size) {
        int limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return jdbcTemplate.query(CHANGES_SQL, (rs, i) -> {
            Timestamp at = rs.getTimestamp("performed_at");
            Number antennaIndex = (Number) rs.getObject("antenna_index");
            return new OperationLogDto(
                    rs.getLong("id"), rs.getString("entity_name"), rs.getString("entity_id"),
                    rs.getString("operation"), at == null ? null : at.toLocalDateTime(), rs.getString("performed_by"),
                    rs.getString("changes"), rs.getString("site_code"), rs.getString("sector_name"),
                    antennaIndex == null ? null : antennaIndex.intValue());
        }, limit, Math.max(page, 0) * limit);
    }

    /** {@code select} returns one {@code name} per row; nulls group as "Not set" */
    private List<NameCount> groups(String[] siteCodes, String select) {
        return jdbcTemplate.query(
                AREA_SITES + "SELECT COALESCE(name, 'Not set') AS name, count(*) AS n FROM (" + select + ") g GROUP BY 1 ORDER BY n DESC, 1",
                (rs, i) -> new NameCount(rs.getString("name"), rs.getLong("n")), (Object) siteCodes);
    }

    private Gap gap(String[] siteCodes, GapQuery q) {
        return jdbcTemplate.queryForObject(
                AREA_SITES + "SELECT count(*) AS n, (array_agg(DISTINCT site_code ORDER BY site_code))[1:" + GAP_SAMPLE + "] AS codes FROM ("
                        + q.sql() + ") g",
                (rs, i) -> {
                    Array codes = rs.getArray("codes");
                    List<String> sample = codes == null ? List.of() : Arrays.asList((String[]) codes.getArray());
                    return new Gap(q.key(), q.label(), rs.getLong("n"), sample);
                }, (Object) siteCodes);
    }
}
