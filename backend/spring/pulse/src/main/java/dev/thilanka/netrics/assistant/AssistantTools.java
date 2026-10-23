package dev.thilanka.netrics.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import dev.thilanka.netrics.assistant.EntityResolver.ClarificationNeeded;
import dev.thilanka.netrics.assistant.EntityResolver.KpiRef;
import dev.thilanka.netrics.dto.AlarmFilter;
import dev.thilanka.netrics.repository.CellRepository;
import dev.thilanka.netrics.service.*;
import dev.thilanka.netrics.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * The only operations the LLM can trigger. Each tool is read-only and maps onto an existing
 * (mostly Redis-cached or index-backed) query, so chat traffic never produces new query shapes.
 */
@Component
public class AssistantTools {
    static final String WORST_CELLS = "get_worst_cells";
    static final String ANOMALIES = "get_anomalies";
    static final String KPI_TREND = "get_kpi_trend";
    static final String AREA_SNAPSHOT = "get_area_kpi_snapshot";
    static final String CELL_KPI = "get_cell_kpi";
    static final String ALARMS = "get_alarms";
    static final String SITE_INFO = "get_site_info";
    static final String CELL_INFO = "get_cell_info";
    private static final Set<String> TOOLS = Set.of(WORST_CELLS, ANOMALIES, KPI_TREND, AREA_SNAPSHOT, CELL_KPI,
            ALARMS, SITE_INFO, CELL_INFO);

    private static final int MAX_ARG_LENGTH = 100;
    private static final List<String> ALARM_SEVERITIES = List.of("CRITICAL", "MAJOR", "MINOR", "WARNING");

    /** A tool call whose parameters are all canonical, validated DB names. */
    public record Call(String tool, Map<String, String> params) {}

    private final EntityResolver resolver;
    private final KpiDayService kpiDayService;
    private final KpiAnomalyService kpiAnomalyService;
    private final AlarmService alarmService;
    private final SiteService siteService;
    private final CellService cellService;
    private final CellRepository cellRepository;
    private final BeamClient beamClient;
    private final SecurityUtils securityUtils;
    private final int maxRows;

    public AssistantTools(EntityResolver resolver, KpiDayService kpiDayService, KpiAnomalyService kpiAnomalyService,
                          AlarmService alarmService, SiteService siteService, CellService cellService,
                          CellRepository cellRepository, BeamClient beamClient, SecurityUtils securityUtils,
                          @Value("${netrics.assistant.max-rows:50}") int maxRows) {
        this.resolver = resolver;
        this.kpiDayService = kpiDayService;
        this.kpiAnomalyService = kpiAnomalyService;
        this.alarmService = alarmService;
        this.siteService = siteService;
        this.cellService = cellService;
        this.cellRepository = cellRepository;
        this.beamClient = beamClient;
        this.securityUtils = securityUtils;
        this.maxRows = maxRows;
    }

    public boolean isKnown(String tool) {
        return TOOLS.contains(tool);
    }

    public Call resolve(String tool, JsonNode args) {
        Map<String, String> p = new LinkedHashMap<>();
        switch (tool) {
            case WORST_CELLS, KPI_TREND -> {
                putKpi(p, resolver.resolveStandardKpi(text(args, "kpi"), text(args, "rat")));
                p.put("area", resolver.resolveArea(text(args, "area")));
                p.put("period", resolver.resolvePeriod(text(args, "period")));
                p.put("granularity", resolver.resolveGranularity(text(args, "granularity")));
                if (tool.equals(WORST_CELLS)) p.put("limit", String.valueOf(limit(args)));
            }
            case AREA_SNAPSHOT -> {
                putKpi(p, resolver.resolveBasicKpi(text(args, "kpi"), text(args, "rat")));
                p.put("area", resolver.resolveArea(text(args, "area")));
                p.put("period", resolver.resolvePeriod(text(args, "period")));
                p.put("granularity", resolver.resolveGranularity(text(args, "granularity")));
            }
            case ANOMALIES -> {
                String kpi = text(args, "kpi");
                if (kpi != null) putKpi(p, resolver.resolveStandardKpi(kpi, text(args, "rat")));
                else p.put("rat", resolver.resolveSingleRat(text(args, "rat")));
                p.put("area", resolver.resolveArea(text(args, "area")));
                p.put("granularity", resolver.resolveGranularity(text(args, "granularity")));
                String severity = text(args, "severity");
                if (severity != null) {
                    severity = severity.toLowerCase();
                    if (!Set.of("critical", "high", "moderate").contains(severity)) {
                        throw new ClarificationNeeded("Which severity?", List.of("critical", "high", "moderate"));
                    }
                    p.put("severity", severity);
                }
                p.put("limit", String.valueOf(limit(args)));
            }
            case CELL_KPI -> {
                putKpi(p, resolver.resolveStandardKpi(text(args, "kpi"), text(args, "rat")));
                String cell = text(args, "cell");
                if (cell == null || !cell.matches(EntityResolver.CELL_NAME_PATTERN)) {
                    throw new ClarificationNeeded("Please give the exact cell name.", List.of());
                }
                p.put("cell", cell);
                p.put("period", resolver.resolvePeriod(text(args, "period")));
                p.put("granularity", resolver.resolveGranularity(text(args, "granularity")));
            }
            case ALARMS -> {
                // Most specific target wins if the model fills several (e.g. "site X in Galle").
                String cell = text(args, "cell"), site = text(args, "site"), area = text(args, "area");
                if (cell != null) p.put("cell", resolver.resolveCell(cell).getCellName());
                else if (site != null) p.put("site", resolver.resolveSite(site));
                else if (area != null) p.put("area", resolver.resolveArea(area));
                else throw new ClarificationNeeded("Which site, cell or area should I check alarms for?", List.of());

                String period = resolver.resolvePeriod(text(args, "period"));
                if (period.equals("month")) {
                    throw new ClarificationNeeded("Alarms can be queried for today or the last 7 days.", List.of("day", "week"));
                }
                p.put("period", period);

                String severity = text(args, "severity");
                if (severity != null) {
                    severity = severity.toUpperCase(Locale.ROOT);
                    if (!ALARM_SEVERITIES.contains(severity)) throw new ClarificationNeeded("Which alarm severity?", ALARM_SEVERITIES);
                    p.put("severity", severity);
                }
                String status = alarmStatus(text(args, "status"));
                if (status != null) p.put("status", status);
                p.put("limit", String.valueOf(limit(args)));
            }
            case SITE_INFO -> p.put("site", resolver.resolveSite(text(args, "site")));
            case CELL_INFO -> p.put("cell", resolver.resolveCell(text(args, "cell")).getCellName());
            default -> throw new IllegalArgumentException("Unknown tool: " + tool);
        }
        return new Call(tool, p);
    }

    /**
     * @param alarmAreaName area used to scope site-level alarm queries (the caller's mapped area, or the
     *                      all-areas area for unrestricted users); the alarms query requires one.
     */
    public List<?> invoke(Call call, String alarmAreaName) {
        Map<String, String> p = call.params();
        return switch (call.tool()) {
            case WORST_CELLS -> kpiDayService.getWorstCellsByKpiAndArea(
                    p.get("kpi"), p.get("period"), true, Integer.parseInt(p.get("limit")),
                    p.get("area"), p.get("rat"), p.get("granularity"));   // impl order: areaName, ratName
            case ANOMALIES -> kpiAnomalyService.getAllAnomalyCellsByArea(
                    "day", p.get("area"), p.get("rat"), p.get("granularity"), p.get("severity"), p.get("kpi"),
                    "severity", "desc", 0, Integer.parseInt(p.get("limit")), null).content();
            case KPI_TREND -> kpiDayService.getTrendByKpiAndArea(
                    p.get("kpi"), p.get("period"), p.get("area"), p.get("rat"), p.get("granularity"));
            case AREA_SNAPSHOT -> List.of(kpiDayService.getLatestBasicAndStandardKpiSnapshotsByArea(
                    p.get("kpi"), p.get("period"), p.get("area"), p.get("rat"), p.get("granularity")));
            case CELL_KPI -> kpiDayService.getDataByKpiAndCell(
                    p.get("kpi"), p.get("cell"), p.get("period"), p.get("rat"), p.get("granularity"));
            case ALARMS -> {
                int limit = Integer.parseInt(p.get("limit"));
                String severity = p.get("severity"), status = p.get("status");
                if (p.containsKey("cell")) {
                    yield alarmService.getAlarmsByCell(p.get("cell"), p.get("period")).stream()
                            .filter(a -> severity == null || severity.equals(a.severity()))
                            .filter(a -> status == null || status.equals(a.clearState()))
                            .limit(limit)
                            .toList();
                }
                AlarmFilter filter = new AlarmFilter(p.get("period"), p.get("site"), severity, null, null, null,
                        status, null, p.getOrDefault("area", alarmAreaName));
                yield alarmService.getAlarms(filter, 0, limit).content();
            }
            case SITE_INFO -> {
                String siteCode = p.get("site");
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("site", siteService.getBySiteCode(siteCode));
                info.put("cells", cellRepository.findCellsBySiteCode(siteCode));
                // Beam checks ROLE_BEAM_READ itself (the user's token is relayed); skip the call when it would be denied.
                info.put("engineering", securityUtils.hasRole("BEAM_READ") ? beamClient.siteEngineeringSummary(siteCode) : null);
                yield List.of(info);
            }
            case CELL_INFO -> List.of(cellService.getByCellName(p.get("cell")));
            default -> throw new IllegalArgumentException("Unknown tool: " + call.tool());
        };
    }

    public List<Map<String, Object>> definitions() {
        Map<String, Object> kpi = str("KPI name exactly as the user wrote it, e.g. 'E-RAB drop rate', 'RRC setup success rate'");
        Map<String, Object> area = str("District or region name, e.g. 'Kandy', 'Region 1'");
        Map<String, Object> rat = str("Radio technology only if the user mentioned it: 2G/GSM, 4G/LTE, LTE-FDD, LTE-TDD, 5G/NR");
        Map<String, Object> period = oneOf("day = latest day (today/yesterday), week = last 7 days, month = last 30 days", "day", "week", "month");
        Map<String, Object> granularity = oneOf("Use busy-hour only if the user asks for busy hour", "day-average", "busy-hour");
        Map<String, Object> limit = Map.of("type", "integer", "description", "Number of rows to return, default 10, max " + maxRows);
        Map<String, Object> site = str("Site code exactly as written, e.g. 'GLAHA4'");
        Map<String, Object> cell = str("Exact cell name");

        return List.of(
                tool(WORST_CELLS, "Top worst performing cells for one KPI in a district or region.",
                        Map.of("kpi", kpi, "area", area, "rat", rat, "period", period, "granularity", granularity, "limit", limit),
                        List.of("kpi", "area")),
                tool(ANOMALIES, "Cells with abnormal/anomalous KPI values in a district or region, optionally for one KPI.",
                        Map.of("kpi", str("KPI name if the user mentioned one, otherwise omit"), "area", area, "rat", rat,
                                "severity", oneOf("Only if the user asked for a severity", "critical", "high", "moderate"),
                                "granularity", granularity, "limit", limit),
                        List.of("area")),
                tool(KPI_TREND, "Trend of one KPI over time for a district or region.",
                        Map.of("kpi", kpi, "area", area, "rat", rat, "period", period, "granularity", granularity),
                        List.of("kpi", "area")),
                tool(AREA_SNAPSHOT, "Current value and change of a KPI category (e.g. Accessibility, Retainability, Integrity) for a district or region.",
                        Map.of("kpi", str("KPI category, e.g. 'Accessibility'"), "area", area, "rat", rat, "period", period, "granularity", granularity),
                        List.of("kpi", "area")),
                tool(CELL_KPI, "Values of one KPI for one specific cell.",
                        Map.of("kpi", kpi, "cell", cell, "rat", rat, "period", period, "granularity", granularity),
                        List.of("kpi", "cell")),
                tool(ALARMS, "Network alarms for a site, a cell, or a district/region. Give exactly one of site, cell or area.",
                        Map.of("site", site, "cell", cell, "area", area,
                                "severity", oneOf("Only if the user asked for a severity", "CRITICAL", "MAJOR", "MINOR", "WARNING"),
                                "status", oneOf("active = uncleared/open alarms, cleared = resolved alarms; omit for all", "active", "cleared"),
                                "period", oneOf("day = today, week = last 7 days", "day", "week"),
                                "limit", limit),
                        List.of()),
                tool(SITE_INFO, "Site details: name, location, its cells (RAT, band, sector, azimuth) and engineering parameters (antennas, tilts, tower).",
                        Map.of("site", site), List.of("site")),
                tool(CELL_INFO, "Details of one cell: site, node, RAT, band, sector, azimuth, carrier.",
                        Map.of("cell", cell), List.of("cell"))
        );
    }

    private static String alarmStatus(String input) {
        if (input == null) return null;
        String n = EntityResolver.norm(input);
        if (Set.of("active", "uncleared", "open", "raised", "ongoing", "current").contains(n)) return "UNCLEARED";
        if (Set.of("cleared", "resolved", "closed").contains(n)) return "CLEARED";
        throw new ClarificationNeeded("Active or cleared alarms?", List.of("active", "cleared"));
    }

    private void putKpi(Map<String, String> p, KpiRef kpi) {
        p.put("kpi", kpi.name());
        p.put("kpiLabel", kpi.label());
        p.put("rat", kpi.rat());
    }

    private int limit(JsonNode args) {
        return Math.max(1, Math.min(maxRows, args.path("limit").asInt(10)));
    }

    private static String text(JsonNode args, String field) {
        JsonNode node = args.path(field);
        if (!node.isValueNode() || node.isNull()) return null;
        String value = node.asText().trim();
        if (value.isEmpty()) return null;
        return value.length() > MAX_ARG_LENGTH ? value.substring(0, MAX_ARG_LENGTH) : value;
    }

    private static Map<String, Object> tool(String name, String description, Map<String, Object> properties, List<String> required) {
        return Map.of("type", "function", "function", Map.of(
                "name", name,
                "description", description,
                "parameters", Map.of("type", "object", "properties", properties, "required", required)));
    }

    private static Map<String, Object> str(String description) {
        return Map.of("type", "string", "description", description);
    }

    private static Map<String, Object> oneOf(String description, String... values) {
        return Map.of("type", "string", "description", description, "enum", List.of(values));
    }
}
