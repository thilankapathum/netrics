package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiSnapshotDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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





    @Query(value = """
            SELECT
            	curr.kpi_label, curr.unit, curr.value,
            	pre.previous_value, (curr.value - pre.previous_value) AS difference,
            		CASE
            			WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
            			WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
            			ELSE 0
            		END AS improved
            FROM (
            	SELECT lte_fdd_standard_kpi.label kpi_label, lte_fdd_standard_kpi.unit unit,
            		lte_fdd_standard_kpi.worst_order worst_order,
            		COALESCE(
            			CASE
            				WHEN lte_fdd_standard_kpi.unit = '%'
            				THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
            				ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
            			END,
            			AVG(kpi_value)
            		) AS value
            	FROM lte_fdd_kpi_day
            	LEFT JOIN lte_fdd_standard_kpi
            		ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            	JOIN district_codes dc
            		ON dc.id = district_code_id
            	JOIN districts d
            		ON d.id = dc.district_id
            	WHERE lte_fdd_standard_kpi_id = :standardKpiId
            		AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
            			AND (:timestamp)
            		AND d.id = :districtId
            	GROUP BY kpi_label
            ) AS curr
            LEFT JOIN (
            	SELECT lte_fdd_standard_kpi.label pre_kpi_label, lte_fdd_standard_kpi.unit unit,
            		COALESCE(
            			CASE
            				WHEN lte_fdd_standard_kpi.unit = '%'
            				THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
            				ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
            			END,
            			AVG(kpi_value)
            		) AS previous_value
            	FROM lte_fdd_kpi_day
            	LEFT JOIN lte_fdd_standard_kpi
            		ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            	JOIN district_codes dc
            		ON dc.id = district_code_id
            	JOIN districts d
            		ON d.id = dc.district_id
            	WHERE lte_fdd_standard_kpi_id = :standardKpiId
            		AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
            			AND (:preTimestamp)
            		AND d.id = :districtId
            	GROUP BY pre_kpi_label
            ) AS pre
            ON curr.kpi_label = pre.pre_kpi_label
            """, nativeQuery = true)
    Optional<KpiSnapshotDto> findLatestKpiSnapshotByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("districtId") Long districtId);


    //  -------------------------- WORST CELLS WITH PREVIOUS - START -------------------------------------------------------


    @Query(value = """
            
            SELECT * FROM
            (SELECT
            	curr.cell_name, curr.kpi_label, curr.unit, curr.value,
            	pre.previous_value, (curr.value - pre.previous_value) AS difference,
            	CASE
            		WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
            		WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
            		ELSE 0
            	END AS improved
            FROM (
            	SELECT cell_name, lte_fdd_standard_kpi.label kpi_label, lte_fdd_standard_kpi.unit unit,
            		lte_fdd_standard_kpi.worst_order worst_order,
            		COALESCE(
            			CASE
            				WHEN lte_fdd_standard_kpi.unit = '%'
            				THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
            				ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
            			END,
            			AVG(kpi_value)
            		) AS value
            	FROM lte_fdd_kpi_day
            	LEFT JOIN lte_fdd_standard_kpi
            		ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            	WHERE lte_fdd_standard_kpi_id = :standardKpiId
            		AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
            			AND (:timestamp)
            	GROUP BY cell_name
            ) AS curr
            LEFT JOIN (
            	SELECT cell_name pre_cell_name, lte_fdd_standard_kpi.unit unit,
            		COALESCE(
            			CASE
            				WHEN lte_fdd_standard_kpi.unit = '%'
            				THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
            				ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
            			END,
            			AVG(kpi_value)
            		) AS previous_value
            	FROM lte_fdd_kpi_day
            	LEFT JOIN lte_fdd_standard_kpi
            		ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            		WHERE lte_fdd_standard_kpi_id = :standardKpiId
            			AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
            				AND (:preTimestamp)
            		GROUP BY pre_cell_name
            	) AS pre
            ON curr.cell_name = pre.pre_cell_name
            ORDER BY
            	CASE WHEN curr.worst_order = 'ASC' THEN curr.value END ASC,
            	CASE WHEN curr.worst_order = 'DESC' THEN curr.value END DESC
            LIMIT 100
            ) AS top100
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM (
                    SELECT curr.cell_name
                    FROM (
                        SELECT cell_name,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%'
                                    THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                                    ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                                END,
                                AVG(kpi_value)
                            ) AS value,
                            lte_fdd_standard_kpi.worst_order
                        FROM lte_fdd_kpi_day
                        LEFT JOIN lte_fdd_standard_kpi
                            ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                                AND (:timestamp)
                        GROUP BY cell_name
                        ORDER BY
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'ASC' THEN value END ASC,
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'DESC' THEN value END DESC
                        LIMIT 100
                    ) AS curr
            ) AS count_query
            """, nativeQuery = true)
    Page<WorstCellsDto> findWorstCells(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, Pageable pageable);


    @Query(value = """
            SELECT * FROM
                        (SELECT
                        	curr.cell_name, curr.kpi_label, curr.unit, curr.value,
                        	pre.previous_value, (curr.value - pre.previous_value) AS difference,
                        	CASE
                        		WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
                        		WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
                        		ELSE 0
                        	END AS improved
                        FROM (
                        	SELECT cell_name, lte_fdd_standard_kpi.label kpi_label, lte_fdd_standard_kpi.unit unit,
                        		lte_fdd_standard_kpi.worst_order worst_order,
                        		COALESCE(
                        			CASE
                        				WHEN lte_fdd_standard_kpi.unit = '%'
                        				THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                        				ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                        			END,
                        			AVG(kpi_value)
                        		) AS value
                        	FROM lte_fdd_kpi_day
                        	LEFT JOIN lte_fdd_standard_kpi
                        		ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            				JOIN district_codes dc
            					ON dc.id = district_code_id
            				JOIN districts d
            					ON d.id = dc.district_id
                        	WHERE lte_fdd_standard_kpi_id = :standardKpiId
                        		AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                        			AND (:timestamp)
            					AND d.id = :districtId
                        	GROUP BY cell_name
                        ) AS curr
                        LEFT JOIN (
                        	SELECT cell_name pre_cell_name, lte_fdd_standard_kpi.unit unit,
                        		COALESCE(
                        			CASE
                        				WHEN lte_fdd_standard_kpi.unit = '%'
                        				THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                        				ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                        			END,
                        			AVG(kpi_value)
                        		) AS previous_value
                        	FROM lte_fdd_kpi_day
                        	LEFT JOIN lte_fdd_standard_kpi
                        		ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
            				JOIN district_codes dc
            					ON dc.id = district_code_id
            				JOIN districts d
            					ON d.id = dc.district_id
            				WHERE lte_fdd_standard_kpi_id = :standardKpiId
            					AND timestamp BETWEEN DATE_SUB(:preTimestamp, INTERVAL :period DAY)
            						AND (:preTimestamp)
            					AND d.id = :districtId
            				GROUP BY pre_cell_name
                        	) AS pre
                        ON curr.cell_name = pre.pre_cell_name
                        ORDER BY
                        	CASE WHEN curr.worst_order = 'ASC' THEN curr.value END ASC,
                        	CASE WHEN curr.worst_order = 'DESC' THEN curr.value END DESC
                        LIMIT 100
            ) AS top100
            """, countQuery = """
            SELECT COUNT(*)
            FROM (
                    SELECT curr.cell_name
                    FROM (
                        SELECT cell_name,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%'
                                    THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                                    ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                                END,
                                AVG(kpi_value)
                            ) AS value,
                            lte_fdd_standard_kpi.worst_order
                        FROM lte_fdd_kpi_day
                        LEFT JOIN lte_fdd_standard_kpi
                            ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                        JOIN district_codes dc
            				ON dc.id = district_code_id
            			JOIN districts d
            				ON d.id = dc.district_id
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                                AND (:timestamp)
                            AND d.id = :districtId
                        GROUP BY cell_name
                        ORDER BY
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'ASC' THEN value END ASC,
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'DESC' THEN value END DESC
                        LIMIT 100
                    ) AS curr
            ) AS count_query
            """, nativeQuery = true)
    Page<WorstCellsDto> findWorstCellsByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, Pageable pageable, @Param("districtId") Long districtId);

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
            JOIN district_codes dc
                ON dc.id = district_code_id
            JOIN districts d
                ON d.id = dc.district_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
                AND d.id = :districtId
            GROUP BY timestamp, lte_fdd_standard_kpi_id;
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataAvgByKpiAndDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("districtId") Long districtId);

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
            JOIN district_codes dc
                ON dc.id = district_code_id
            JOIN districts d
                ON d.id = dc.district_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
            AND timestamp BETWEEN DATE_SUB(:timestamp, INTERVAL :period DAY)
                    AND (:timestamp)
                AND d.id = :districtId
            GROUP BY timestamp, lte_fdd_standard_kpi_id;
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataSumByKpiAndDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("districtId") Long districtId);






    @Query(value = """
            SELECT * FROM lte_fdd_kpi_day
            WHERE district_code_id IS NULL
            """, nativeQuery = true)
    List<LteFddKpiDay> findKpiWithoutDistrict();
}
