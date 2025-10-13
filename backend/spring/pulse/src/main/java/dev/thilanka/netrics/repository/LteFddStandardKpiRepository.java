package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LteFddStandardKpiRepository extends JpaRepository<LteFddStandardKpi, Long> {
    Optional<LteFddStandardKpi> findByKpiNameAndRatId(String kpiName, Long ratId);

    Optional<LteFddStandardKpi> findByLabel(String label);

    @Query(value = """
            SELECT * FROM lte_fdd_standard_kpi
            WHERE type = 'standard' AND rat_id = :ratId
            """, nativeQuery = true)
    List<LteFddStandardKpi> findAllStandardKpiByRat(@Param("ratId") Long ratId);
}
