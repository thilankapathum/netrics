package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.KpiDataWithOperandsDto;
import dev.thilanka.netrics.entity.KpiData;
import dev.thilanka.netrics.entity.KpiHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiHourRepository extends JpaRepository<KpiHour, Long> {

    @Query(value = "SELECT DISTINCT timestamp FROM kpi_values_hour WHERE kpi_values_hour.rat_id = :ratId AND kpi_values_hour.granularity_id = :granularityId ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate(@Param("ratId") Long ratId, @Param("granularityId") Long granularityId);



    // ----------------------------- KPI TREND DATA BY CELL AND KPI ----------------------------------------------------------


    @Query(value = """
        SELECT
            "timestamp",
            cell_name,
            standard_kpi.label AS kpiLabel,
            kpi_value
        FROM kpi_values_hour
        LEFT JOIN standard_kpi
            ON standard_kpi.id = kpi_values_hour.standard_kpi_id
        WHERE standard_kpi_id = :standardKpiId
            AND cell_name = :cellName
            AND "timestamp" BETWEEN :startTimestamp AND :timestamp
            AND kpi_values_hour.rat_id = :ratId
            AND kpi_values_hour.granularity_id = :granularityId
        ORDER BY timestamp ASC
        """, nativeQuery = true)
    List<KpiData> findDataByKpiAndCell(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("cellName") String cellName, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


    @Query(value = """
        SELECT
            "timestamp",
            cell_name,
            standard_kpi.label AS label,
            kpi_value,
            numerator_kpi_value,
            denominator_kpi_value
        FROM kpi_values_hour
        LEFT JOIN standard_kpi
            ON standard_kpi.id = kpi_values_hour.standard_kpi_id
        WHERE standard_kpi_id = :standardKpiId
            AND cell_name = :cellName
            AND "timestamp" BETWEEN :startTimestamp AND :timestamp
            AND kpi_values_hour.rat_id = :ratId
            AND kpi_values_hour.granularity_id = :granularityId
        ORDER BY timestamp ASC
        """, nativeQuery = true)
    List<KpiDataWithOperandsDto> findDataByKpiAndCellWithOperands(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("cellName") String cellName, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


}
