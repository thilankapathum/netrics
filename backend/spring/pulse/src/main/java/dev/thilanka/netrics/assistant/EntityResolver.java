package dev.thilanka.netrics.assistant;

import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.repository.AreaRepository;
import dev.thilanka.netrics.repository.BasicKpiRepository;
import dev.thilanka.netrics.repository.RatRepository;
import dev.thilanka.netrics.repository.StandardKpiRepository;
import lombok.Getter;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministically maps the free-text values the LLM extracted ("e-rab drop", "Kandy district",
 * "yesterday", "4G") onto canonical DB names. The LLM is never trusted to produce exact names;
 * anything unresolvable or ambiguous becomes a clarification instead of a DB query.
 */
@Component
public class EntityResolver {

    public static class ClarificationNeeded extends RuntimeException {
        @Getter
        private final List<String> options;

        public ClarificationNeeded(String message, List<String> options) {
            super(message);
            this.options = options;
        }
    }

    public record KpiRef(String name, String label, String rat) {}

    private record Catalog(List<KpiRef> standardKpis, List<KpiRef> basicKpis, List<String> areas,
                           Map<String, String> ratLabels, Map<String, String> kpiAliases, Instant loadedAt) {}

    private static final Duration CATALOG_TTL = Duration.ofHours(1);
    private static final Map<String, String> RAT_ALIASES = Map.of("2g", "gsm", "3g", "umts", "4g", "lte", "5g", "nr");
    private static final int MAX_OPTIONS = 10;

    private final StandardKpiRepository standardKpiRepository;
    private final BasicKpiRepository basicKpiRepository;
    private final AreaRepository areaRepository;
    private final RatRepository ratRepository;
    private final Environment environment;
    private volatile Catalog catalog;

    public EntityResolver(StandardKpiRepository standardKpiRepository, BasicKpiRepository basicKpiRepository,
                          AreaRepository areaRepository, RatRepository ratRepository, Environment environment) {
        this.standardKpiRepository = standardKpiRepository;
        this.basicKpiRepository = basicKpiRepository;
        this.areaRepository = areaRepository;
        this.ratRepository = ratRepository;
        this.environment = environment;
    }

    public List<String> areaNames() {
        return catalog().areas();
    }

    public Collection<String> ratLabels() {
        return catalog().ratLabels().values();
    }

    public String ratLabel(String ratName) {
        return catalog().ratLabels().getOrDefault(ratName, ratName);
    }

    /** @return candidate RAT names; all RATs when not specified. */
    public List<String> resolveRats(String input) {
        Map<String, String> rats = catalog().ratLabels();
        if (input == null) return List.copyOf(rats.keySet());

        String n = RAT_ALIASES.getOrDefault(norm(input), norm(input));
        List<String> exact = rats.entrySet().stream()
                .filter(e -> norm(e.getKey()).equals(n) || norm(e.getValue()).equals(n))
                .map(Map.Entry::getKey).toList();
        if (!exact.isEmpty()) return exact;

        List<String> prefix = rats.keySet().stream().filter(r -> !n.isEmpty() && norm(r).startsWith(n)).toList();  // "lte" -> ltefdd, ltetdd
        if (prefix.isEmpty()) throw new ClarificationNeeded("Unknown radio technology '" + input + "'. Which one did you mean?", List.copyOf(rats.values()));
        return prefix;
    }

    /** Exactly one RAT, or a clarification. */
    public String resolveSingleRat(String input) {
        List<String> rats = resolveRats(input);
        if (rats.size() > 1) throw new ClarificationNeeded("Which radio technology?", rats.stream().map(this::ratLabel).toList());
        return rats.get(0);
    }

    public KpiRef resolveStandardKpi(String input, String ratInput) {
        return resolveKpi(input, ratInput, catalog().standardKpis());
    }

    public KpiRef resolveBasicKpi(String input, String ratInput) {
        return resolveKpi(input, ratInput, catalog().basicKpis());
    }

    private KpiRef resolveKpi(String input, String ratInput, List<KpiRef> all) {
        if (input == null) throw new ClarificationNeeded("Which KPI?", List.of());
        List<String> rats = resolveRats(ratInput);
        List<KpiRef> pool = all.stream().filter(k -> rats.contains(k.rat())).toList();

        String alias = catalog().kpiAliases().get(norm(input));
        String n = norm(alias != null ? alias : input);

        List<KpiRef> hits = pool.stream().filter(k -> norm(k.name()).equals(n) || norm(k.label()).equals(n)).toList();
        if (hits.isEmpty() && n.length() >= 3) {
            hits = pool.stream().filter(k -> norm(k.name()).contains(n) || norm(k.label()).contains(n)).toList();
        }

        if (hits.isEmpty()) {
            Set<String> words = Arrays.stream(input.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                    .filter(w -> w.length() >= 3).collect(Collectors.toSet());
            List<String> suggestions = pool.stream().map(KpiRef::label)
                    .filter(l -> words.stream().anyMatch(w -> l.toLowerCase(Locale.ROOT).contains(w)))
                    .distinct().limit(MAX_OPTIONS).toList();
            throw new ClarificationNeeded("I couldn't find a KPI matching '" + input + "'.", suggestions);
        }
        if (hits.size() == 1) return hits.get(0);

        List<String> labels = hits.stream().map(KpiRef::label).distinct().toList();
        if (labels.size() == 1) {
            throw new ClarificationNeeded(labels.get(0) + " exists for several radio technologies. Which one?",
                    hits.stream().map(k -> ratLabel(k.rat())).distinct().toList());
        }
        throw new ClarificationNeeded("Which KPI did you mean?", labels.stream().limit(MAX_OPTIONS).toList());
    }

    public String resolveArea(String input) {
        List<String> areas = catalog().areas();
        if (input == null) throw new ClarificationNeeded("Which district or region?", areas);

        String n = norm(input);
        List<String> hits = areas.stream().filter(a -> norm(a).equals(n)).toList();
        String stripped = norm(input.replaceAll("(?i)\\b(district|area|province)\\b", ""));
        if (hits.isEmpty()) hits = areas.stream().filter(a -> norm(a).equals(stripped)).toList();
        if (hits.isEmpty() && stripped.length() >= 3) hits = areas.stream().filter(a -> norm(a).contains(stripped)).toList();

        if (hits.size() == 1) return hits.get(0);
        if (hits.isEmpty()) throw new ClarificationNeeded("I couldn't find an area named '" + input + "'.", areas);
        throw new ClarificationNeeded("Which area did you mean?", hits);
    }

    public String resolvePeriod(String input) {
        if (input == null) return "day";
        String n = norm(input);
        if (n.contains("month")) return "month";
        if (n.contains("week")) return "week";
        if (n.contains("day") || n.equals("today") || n.equals("latest")) return "day";   // day, yesterday, today, daily
        throw new ClarificationNeeded("Only the latest day, week or month can be queried.", List.of("day", "week", "month"));
    }

    public String resolveGranularity(String input) {
        if (input == null) return "day-average";
        String n = norm(input);
        if (n.contains("busy")) return "busy-hour";
        if (n.contains("day") || n.contains("average")) return "day-average";
        throw new ClarificationNeeded("Only day-average or busy-hour data can be queried.", List.of("day-average", "busy-hour"));
    }

    static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private Catalog catalog() {
        Catalog c = catalog;
        if (c == null || c.loadedAt().isBefore(Instant.now().minus(CATALOG_TTL))) {
            c = load();
            catalog = c;
        }
        return c;
    }

    private Catalog load() {
        List<KpiRef> standard = standardKpiRepository.findAll().stream()
                .filter(k -> "standard".equals(k.getType()) && k.getRat() != null)
                .map(k -> new KpiRef(k.getKpiName(), k.getLabel(), k.getRat().getName())).toList();
        List<KpiRef> basic = basicKpiRepository.findAll().stream()
                .filter(k -> k.getRat() != null)
                .map(k -> new KpiRef(k.getKpiName(), k.getLabel(), k.getRat().getName())).toList();
        List<String> areas = areaRepository.findAll().stream().filter(Area::isEnabled).map(Area::getName).sorted().toList();
        Map<String, String> rats = ratRepository.findAll().stream()
                .collect(Collectors.toMap(Rat::getName, Rat::getLabel, (a, b) -> a, LinkedHashMap::new));
        Map<String, String> aliases = Binder.get(environment)
                .bind("netrics.assistant.kpi-aliases", Bindable.mapOf(String.class, String.class))
                .orElse(Map.of()).entrySet().stream()
                .collect(Collectors.toMap(e -> norm(e.getKey()), Map.Entry::getValue, (a, b) -> a));
        return new Catalog(standard, basic, areas, rats, aliases, Instant.now());
    }
}
