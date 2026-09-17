package dev.thilanka.netrics.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import dev.thilanka.netrics.assistant.EntityResolver.ClarificationNeeded;
import dev.thilanka.netrics.assistant.EntityResolver.KpiRef;
import dev.thilanka.netrics.service.KpiAnomalyService;
import dev.thilanka.netrics.service.KpiDayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The only operations the LLM can trigger. Each tool is read-only and maps onto an existing
 * (mostly Redis-cached) service method, so chat traffic never produces new query shapes.
 */
@Component
public class AssistantTools {
    static final String WORST_CELLS = "get_worst_cells";
    static final String ANOMALIES = "get_anomalies";
    static final String KPI_TREND = "get_kpi_trend";
    static final String AREA_SNAPSHOT = "get_area_kpi_snapshot";
    static final String CELL_KPI = "get_cell_kpi";
    private static final Set<String> TOOLS = Set.of(WORST_CELLS, ANOMALIES, KPI_TREND, AREA_SNAPSHOT, CELL_KPI);

    private static final int MAX_ARG_LENGTH = 100;
    private static final String CELL_NAME_PATTERN = "[A-Za-z0-9_.\\-]{1,64}";

    /** A tool call whose parameters are all canonical, validated DB names. */
    public record Call(String tool, Map<String, String> params) {}

    private final EntityResolver resolver;
    private final KpiDayService kpiDayService;
    private final KpiAnomalyService kpiAnomalyService;
    private final int maxRows;

    public AssistantTools(EntityResolver resolver, KpiDayService kpiDayService, KpiAnomalyService kpiAnomalyService,
                          @Value("${netrics.assistant.max-rows:50}") int maxRows) {
        this.resolver = resolver;
        this.kpiDayService = kpiDayService;
        this.kpiAnomalyService = kpiAnomalyService;
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
                if (cell == null || !cell.matches(CELL_NAME_PATTERN)) {
                    throw new ClarificationNeeded("Please give the exact cell name.", List.of());
                }
                p.put("cell", cell);
                p.put("period", resolver.resolvePeriod(text(args, "period")));
                p.put("granularity", resolver.resolveGranularity(text(args, "granularity")));
            }
            default -> throw new IllegalArgumentException("Unknown tool: " + tool);
        }
        return new Call(tool, p);
    }

    public List<?> invoke(Call call) {
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
            default -> throw new IllegalArgumentException("Unknown tool: " + call.tool());
        };
    }

    public List<Map<String, Object>> definitions() {
        Map<String, Object> kpi = str("KPI name exactly as the user wrote it, e.g. 'E-RAB drop rate', 'RRC setup success rate'");
        Map<String, Object> area = str("District or region name, e.g. 'Kandy', 'Region 1'");
        Map<String, Object> rat = str("Radio technology only if the user mentioned it: 2G/GSM, 4G/LTE, LTE-FDD, LTE-TDD, 5G/NR");
        Map<String, Object> period = oneOf("day = latest day (today/yesterday), week = last 7 days, month = last 30 days", "day", "week", "month");
        Map<String, Object> granularity = oneOf("Use busy-hour only if the user asks for busy hour", "day-average", "busy-hour");
        Map<String, Object> limit = Map.of("type", "integer", "description", "Number of cells to return, default 10, max " + maxRows);

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
                        Map.of("kpi", kpi, "cell", str("Exact cell name"), "rat", rat, "period", period, "granularity", granularity),
                        List.of("kpi", "cell"))
        );
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
