package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardRawKpiMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StandardRawKpiMappingRepository extends JpaRepository<StandardRawKpiMapping, Long> {

    List<StandardRawKpiMapping> findByRat(Rat rat);

    @Query(value = """
            SELECT * FROM public.standard_raw_kpi_mapping
            WHERE rat_id = :ratId
            	AND standard_kpi_id = :standardKpiId;
            """, nativeQuery = true)
    List<StandardRawKpiMapping> findByRatAndKpi(@Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId);
}
