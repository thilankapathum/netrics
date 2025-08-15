package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.CalculatedKpiSnapshot;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import dev.thilanka.netrics.entity.ltefdd.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LteFddKpiDayRepository extends JpaRepository<LteFddKpiDay, Long> {


    @Query(value = "SELECT lte_fdd_standard_kpi_id, \n" +
            "SUM(kpi_value) kpi_value_sum, \n" +
            "SUM(numerator_kpi_value) numerator_kpi_value_sum, \n" +
            "SUM(denominator_kpi_value) denominator_kpi_value_sum FROM lte_fdd_kpi_day\n" +
            "WHERE timestamp = '2025-07-20 00.00.00'\n" +
            "AND lte_fdd_standard_kpi_id = 10", nativeQuery = true)
    List<KpiSnapshot> getKpiX();

    @Query(value = "SELECT lte_fdd_standard_kpi.label label, \n" +
            "SUM(kpi_value) kpi_value_sum, \n" +
            "SUM(numerator_kpi_value) numerator_kpi_value_sum, \n" +
            "SUM(denominator_kpi_value) denominator_kpi_value_sum \n"+
            "FROM lte_fdd_kpi_day LEFT JOIN lte_fdd_standard_kpi\n" +
            "ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id\n" +
            "WHERE timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)\n" +
            "AND :timestamp \n" +
            "AND lte_fdd_standard_kpi_id = :standardKpiId", nativeQuery = true)
    Optional<KpiSnapshot> findLatestKpiSnapshot(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period);

    @Query(value = "SELECT DISTINCT timestamp FROM lte_fdd_kpi_day ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate();

    @Query(value = "SELECT \n" +
            "    lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.worst_order worst_order, \n" +
            "    SUM(kpi_value) kpi_value_sum,\n" +
            "    (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value\n" +
            "FROM\n" +
            "    lte_fdd_kpi_day\n" +
            "        LEFT JOIN\n" +
            "    lte_fdd_standard_kpi ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id\n" +
            "WHERE\n" +
            "    timestamp BETWEEN DATE_SUB(:timestamp,\n" +
            "        INTERVAL :period DAY) AND :timestamp\n" +
            "        AND lte_fdd_standard_kpi_id = :standardKpiId",nativeQuery = true)
    Optional<CalculatedKpiSnapshot> findLatestCalculatedKpiSnapshot(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period);

}
