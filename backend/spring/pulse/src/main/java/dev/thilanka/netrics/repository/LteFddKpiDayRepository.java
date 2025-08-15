package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.KpiSnapshot;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LteFddKpiDayRepository extends JpaRepository<LteFddKpiDay, Long> {

    @Query(value = "SELECT DISTINCT timestamp FROM lte_fdd_kpi_day ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate();

    @Query(value = "SELECT lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.worst_order worst_order, " +
            "SUM(kpi_value) kpi_value_sum, " +
            "(SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value " +
            "FROM lte_fdd_kpi_day " +
            "LEFT JOIN lte_fdd_standard_kpi " +
            "ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id " +
            "WHERE timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY) " +
            "AND :timestamp " +
            "AND lte_fdd_standard_kpi_id = :standardKpiId",nativeQuery = true)
    Optional<KpiSnapshot> findLatestCalculatedKpiSnapshot(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period);

}
