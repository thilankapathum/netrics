package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StandardKpiRepository extends JpaRepository<StandardKpi, Long> {
    Optional<StandardKpi> findByKpiNameAndRatId(String kpiName, Long ratId);

    Optional<StandardKpi> findByLabelAndRat(String label, Rat rat);

    @Query(value = """
            SELECT * FROM lte_fdd_standard_kpi
            WHERE type = 'standard' AND rat_id = :ratId
            """, nativeQuery = true)
    List<StandardKpi> findAllStandardKpiByRat(@Param("ratId") Long ratId);
}
