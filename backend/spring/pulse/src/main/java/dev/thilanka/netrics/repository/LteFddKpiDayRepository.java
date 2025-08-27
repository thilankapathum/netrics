package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.KpiData;
import dev.thilanka.netrics.entity.KpiSnapshotCurrentPre;
import dev.thilanka.netrics.entity.KpiTrend;
import dev.thilanka.netrics.entity.WorstCellCurrentPre;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LteFddKpiDayRepository extends JpaRepository<LteFddKpiDay, Long> {

    @Query(value = "SELECT DISTINCT timestamp FROM lte_fdd_kpi_day ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate();


    @Query(value = """
            SELECT curr.label, curr.unit, curr.worst_order, curr.kpi_value, curr.calculated_kpi_value,
            pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM
            
            (SELECT lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.unit unit, lte_fdd_standard_kpi_id id, lte_fdd_standard_kpi.worst_order worst_order,
            	AVG(kpi_value) kpi_value,
            	(SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
            	ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            WHERE timestamp BETWEEN DATE_SUB(:timestamp , INTERVAL :period DAY)
            	AND :timestamp
            AND lte_fdd_standard_kpi_id = :standardKpiId) curr
            
            LEFT JOIN
            
            (SELECT lte_fdd_standard_kpi_id id,
            	AVG(kpi_value) pre_kpi_value,
            	(SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS pre_calculated_kpi_value
            FROM lte_fdd_kpi_day
            WHERE timestamp BETWEEN DATE_SUB(:PreTimestamp , INTERVAL :period DAY)
            	AND :PreTimestamp
            AND lte_fdd_standard_kpi_id = :standardKpiId) pre
            
            ON curr.id = pre.id
            """, nativeQuery = true)
    Optional<KpiSnapshotCurrentPre> findLatestAvgKpiSnapshotWithPre(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("PreTimestamp") LocalDateTime preTimestamp, @Param("period") Long period);


    @Query(value = """
            SELECT curr.label, curr.unit, curr.worst_order, curr.kpi_value, curr.calculated_kpi_value,
            pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM
            
            (SELECT lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.unit unit, lte_fdd_standard_kpi_id id, lte_fdd_standard_kpi.worst_order worst_order,
            	SUM(kpi_value) kpi_value,
            	(SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
            	ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            WHERE timestamp BETWEEN DATE_SUB(:timestamp , INTERVAL :period DAY)
            	AND :timestamp
            AND lte_fdd_standard_kpi_id = :standardKpiId) curr
            
            LEFT JOIN
            
            (SELECT lte_fdd_standard_kpi_id id,
            	SUM(kpi_value) pre_kpi_value,
            	(SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS pre_calculated_kpi_value
            FROM lte_fdd_kpi_day
            WHERE timestamp BETWEEN DATE_SUB(:PreTimestamp , INTERVAL :period DAY)
            	AND :PreTimestamp
            AND lte_fdd_standard_kpi_id = :standardKpiId) pre
            
            ON curr.id = pre.id
            """, nativeQuery = true)
    Optional<KpiSnapshotCurrentPre> findLatestSumKpiSnapshotWithPre(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("PreTimestamp") LocalDateTime preTimestamp, @Param("period") Long period);


    //  -------------------------- WORST CELLS WITH PREVIOUS - START -------------------------------------------------------

    @Query(value = """
            SELECT
                curr.cell_name, curr.label, curr.unit, curr.kpi_value, curr.calculated_kpi_value,
                pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM (
                SELECT cell_name, lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.unit unit,
            	AVG(kpi_value) kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value
                FROM lte_fdd_kpi_day
                LEFT JOIN lte_fdd_standard_kpi
                        ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
                GROUP BY cell_name
            ) curr
           \s
            LEFT JOIN (
                SELECT cell_name pre_cell_name,
            	AVG(kpi_value) pre_kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS pre_calculated_kpi_value
                FROM lte_fdd_kpi_day
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
                    AND (:preTimestamp)
                GROUP BY pre_cell_name
            ) pre
           \s
            ON curr.cell_name = pre.pre_cell_name
            ORDER BY 
                CASE
                    WHEN curr.calculated_kpi_value IS NOT NULL THEN curr.calculated_kpi_value
                    ELSE curr.kpi_value
                END ASC
            LIMIT :count
           \s""", nativeQuery = true)
    List<WorstCellCurrentPre> findWorstCellsWithPrevAvgAsc(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("count") int count);

    @Query(value = """
            SELECT
                curr.cell_name, curr.label, curr.unit, curr.kpi_value, curr.calculated_kpi_value,
                pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM (
                SELECT cell_name, lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.unit unit,
            	AVG(kpi_value) kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value
                FROM lte_fdd_kpi_day
                LEFT JOIN lte_fdd_standard_kpi
                        ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
                GROUP BY cell_name
            ) curr
            
            LEFT JOIN (
                SELECT cell_name pre_cell_name,
            	AVG(kpi_value) pre_kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS pre_calculated_kpi_value
                FROM lte_fdd_kpi_day
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
                    AND (:preTimestamp)
                GROUP BY pre_cell_name
            ) pre
            
            ON curr.cell_name = pre.pre_cell_name
            ORDER BY
                CASE
                    WHEN curr.calculated_kpi_value IS NOT NULL THEN curr.calculated_kpi_value
                    ELSE curr.kpi_value
                END DESC
            LIMIT :count
            """, nativeQuery = true)
    List<WorstCellCurrentPre> findWorstCellsWithPrevAvgDesc(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("count") int count);

    @Query(value = """
            SELECT
                curr.cell_name, curr.label, curr.unit, curr.kpi_value, curr.calculated_kpi_value,
                pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM (
                SELECT cell_name, lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.unit unit,
            	SUM(kpi_value) kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value
                FROM lte_fdd_kpi_day
                LEFT JOIN lte_fdd_standard_kpi
                        ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
                GROUP BY cell_name
            ) curr
            
            LEFT JOIN (
                SELECT cell_name pre_cell_name,
            	SUM(kpi_value) pre_kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS pre_calculated_kpi_value
                FROM lte_fdd_kpi_day
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
                    AND (:preTimestamp)
                GROUP BY pre_cell_name
            ) pre
            
            ON curr.cell_name = pre.pre_cell_name
            ORDER BY
                CASE
                    WHEN curr.calculated_kpi_value IS NOT NULL THEN curr.calculated_kpi_value
                    ELSE curr.kpi_value
                END ASC
            LIMIT :count
            """, nativeQuery = true)
    List<WorstCellCurrentPre> findWorstCellsWithPrevSumAsc(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("count") int count);

    @Query(value = """
            SELECT
                curr.cell_name, curr.label, curr.unit, curr.kpi_value, curr.calculated_kpi_value,
                pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM (
                SELECT cell_name, lte_fdd_standard_kpi.label label, lte_fdd_standard_kpi.unit unit,
            	SUM(kpi_value) kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS calculated_kpi_value
                FROM lte_fdd_kpi_day
                LEFT JOIN lte_fdd_standard_kpi
                        ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
                GROUP BY cell_name
            ) curr
            
            LEFT JOIN (
                SELECT cell_name pre_cell_name,
            	SUM(kpi_value) pre_kpi_value,
                (SUM(numerator_kpi_value) / SUM(denominator_kpi_value)) AS pre_calculated_kpi_value
                FROM lte_fdd_kpi_day
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
                    AND (:preTimestamp)
                GROUP BY pre_cell_name
            ) pre
            
            ON curr.cell_name = pre.pre_cell_name
            ORDER BY
                CASE
                    WHEN curr.calculated_kpi_value IS NOT NULL THEN curr.calculated_kpi_value
                    ELSE curr.kpi_value
                END DESC
            LIMIT :count
            """, nativeQuery = true)
    List<WorstCellCurrentPre> findWorstCellsWithPrevSumDesc(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("count") int count);


    // ----------------------------- KPI DATA BY CELL AND KPI ----------------------------------------------------------


    @Query(value = """
            SELECT timestamp, cell_name,  lte_fdd_standard_kpi.label label, kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
            	ON lte_fdd_standard_kpi.id = lte_fdd_kpi_day.lte_fdd_standard_kpi_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
            	AND cell_name = :cellName
            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
            """, nativeQuery = true)
    List<KpiData> findDataByKpiAndCell(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("cellName") String cellName);



    // ----------------------------- KPI TREND DATA BY KPI ----------------------------------------------------------


    @Query(value = """
            SELECT timestamp,
            	lte_fdd_standard_kpi.label kpi_label,
                COALESCE(
                    CASE
                    	WHEN lte_fdd_standard_kpi.unit = '%'
                        THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                        ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                    END,
                    AVG(kpi_value)
                ) AS kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
                ON lte_fdd_standard_kpi.id = lte_fdd_standard_kpi_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                                AND (:timestamp)
            GROUP BY timestamp, lte_fdd_standard_kpi_id;
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataAvgByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period);

    @Query(value = """
            SELECT timestamp,
            	lte_fdd_standard_kpi.label kpi_label,
                COALESCE(
                    CASE
                    	WHEN lte_fdd_standard_kpi.unit = '%'
                        THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                        ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                    END,
                    SUM(kpi_value)
                ) AS kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
                ON lte_fdd_standard_kpi.id = lte_fdd_standard_kpi_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                                AND (:timestamp)
            GROUP BY timestamp, lte_fdd_standard_kpi_id;
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataSumByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period);
}
