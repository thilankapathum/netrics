package dev.thilanka.netrics.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.repository.AreaRepository;
import dev.thilanka.netrics.repository.BasicKpiRepository;
import dev.thilanka.netrics.repository.RatRepository;
import dev.thilanka.netrics.repository.StandardKpiRepository;
import dev.thilanka.netrics.service.KpiAnomalyService;
import dev.thilanka.netrics.service.KpiDayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssistantToolsTest {

    private final ObjectMapper json = new ObjectMapper();
    private AssistantTools tools;

    @BeforeEach
    void setUp() {
        Rat fdd = Rat.builder().name("ltefdd").label("LTE-FDD").build();
        Rat tdd = Rat.builder().name("ltetdd").label("LTE-TDD").build();
        Rat nr = Rat.builder().name("nr").label("NR").build();

        StandardKpiRepository kpis = mock(StandardKpiRepository.class);
        when(kpis.findAll()).thenReturn(List.of(
                kpi("erab_drop_rate", "E-RAB Drop Rate", fdd),
                kpi("rrc_setup_sr", "RRC Setup Success Rate", fdd),
                kpi("rrc_setup_sr", "RRC Setup Success Rate", tdd),
                kpi("nr_drop_rate", "SgNB Drop Rate", nr)));
        BasicKpiRepository basics = mock(BasicKpiRepository.class);
        when(basics.findAll()).thenReturn(List.of());
        AreaRepository areas = mock(AreaRepository.class);
        when(areas.findAll()).thenReturn(List.of(area("Kandy"), area("Region 1"), area("Region 10"), area("Galle")));
        RatRepository rats = mock(RatRepository.class);
        when(rats.findAll()).thenReturn(List.of(fdd, tdd, nr));

        MockEnvironment env = new MockEnvironment().withProperty("netrics.assistant.kpi-aliases.erab-drop", "E-RAB Drop Rate");
        EntityResolver resolver = new EntityResolver(kpis, basics, areas, rats, env);
        tools = new AssistantTools(resolver, mock(KpiDayService.class), mock(KpiAnomalyService.class), 50);
    }

    @Test
    void worstCells_resolvesFreeTextToCanonicalNamesAndCapsLimit() {
        AssistantTools.Call call = tools.resolve(AssistantTools.WORST_CELLS,
                args("{\"kpi\":\"e-rab drop rate\",\"area\":\"Kandy district\",\"period\":\"yesterday\",\"limit\":1000}"));

        assertThat(call.params())
                .containsEntry("kpi", "erab_drop_rate")
                .containsEntry("rat", "ltefdd")
                .containsEntry("area", "Kandy")
                .containsEntry("period", "day")
                .containsEntry("granularity", "day-average")
                .containsEntry("limit", "50");
    }

    @Test
    void kpiAlias_isApplied() {
        AssistantTools.Call call = tools.resolve(AssistantTools.KPI_TREND, args("{\"kpi\":\"ERAB drop\",\"area\":\"kandy\"}"));
        assertThat(call.params()).containsEntry("kpi", "erab_drop_rate");
    }

    @Test
    void kpiInSeveralRats_asksWhichRat() {
        assertThatThrownBy(() -> tools.resolve(AssistantTools.ANOMALIES, args("{\"kpi\":\"rrc setup success rate\",\"area\":\"Region 1\"}")))
                .isInstanceOf(EntityResolver.ClarificationNeeded.class)
                .satisfies(e -> assertThat(((EntityResolver.ClarificationNeeded) e).getOptions()).containsExactly("LTE-FDD", "LTE-TDD"));
    }

    @Test
    void ratAlias_narrowsKpi() {
        AssistantTools.Call call = tools.resolve(AssistantTools.ANOMALIES,
                args("{\"kpi\":\"rrc setup success rate\",\"area\":\"Region 1\",\"rat\":\"lte-tdd\",\"severity\":\"CRITICAL\"}"));
        assertThat(call.params()).containsEntry("rat", "ltetdd").containsEntry("area", "Region 1").containsEntry("severity", "critical");
    }

    @Test
    void unknownArea_andUnsupportedPeriod_andBadCellName_needClarification() {
        assertThatThrownBy(() -> tools.resolve(AssistantTools.WORST_CELLS, args("{\"kpi\":\"e-rab drop rate\",\"area\":\"Mars\"}")))
                .isInstanceOf(EntityResolver.ClarificationNeeded.class);
        assertThatThrownBy(() -> tools.resolve(AssistantTools.WORST_CELLS, args("{\"kpi\":\"e-rab drop rate\",\"area\":\"Galle\",\"period\":\"year\"}")))
                .isInstanceOf(EntityResolver.ClarificationNeeded.class);
        assertThatThrownBy(() -> tools.resolve(AssistantTools.CELL_KPI, args("{\"kpi\":\"e-rab drop rate\",\"cell\":\"x'; drop table cells;--\"}")))
                .isInstanceOf(EntityResolver.ClarificationNeeded.class);
    }

    @Test
    void unknownTool_isRejected() {
        assertThat(tools.isKnown("delete_cells")).isFalse();
        assertThatThrownBy(() -> tools.resolve("delete_cells", args("{}"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stripThink_removesReasoningBlock() {
        assertThat(AssistantService.stripThink("<think>\nhmm\n</think>\n\nKandy has 3 bad cells.")).isEqualTo("Kandy has 3 bad cells.");
    }

    private JsonNode args(String raw) {
        try {
            return json.readTree(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static StandardKpi kpi(String name, String label, Rat rat) {
        return StandardKpi.builder().kpiName(name).label(label).type("standard").rat(rat).build();
    }

    private static Area area(String name) {
        return Area.builder().name(name).enabled(true).build();
    }
}
