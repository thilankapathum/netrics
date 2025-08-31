package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LteFddStandardKpiRepository extends JpaRepository<LteFddStandardKpi, Long> {
    Optional<LteFddStandardKpi> findByKpiName(String kpiName);

    Optional<LteFddStandardKpi> findByLabel(String label);

    @Query(value = """
            SELECT * FROM netrics_pulse_db.lte_fdd_standard_kpi
            WHERE type = 'standard'
            """, nativeQuery = true)
    List<LteFddStandardKpi> findAllStandardKpi();
}
