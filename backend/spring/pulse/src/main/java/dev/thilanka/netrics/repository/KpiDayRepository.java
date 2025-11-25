package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.dto.DashboardWorstCellDto;
import dev.thilanka.netrics.dto.KpiSnapshotDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.KpiDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KpiDayRepository extends JpaRepository<KpiDay, Long> {

    @Query(value = "SELECT DISTINCT timestamp FROM lte_fdd_kpi_day WHERE lte_fdd_kpi_day.rat_id = :ratId ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate(@Param("ratId") Long ratId);


    @Query(value = """
            SELECT curr.label, curr.unit, curr.worst_order, curr.kpi_value, curr.calculated_kpi_value,
                   pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM
            (
                SELECT lte_fdd_standard_kpi.label AS label,
                       lte_fdd_standard_kpi.unit AS unit,
                       lte_fdd_standard_kpi.id AS id,
                       lte_fdd_standard_kpi.worst_order AS worst_order,
                       AVG(kpi_value) AS kpi_value,
                       SUM(numerator_kpi_value) / SUM(denominator_kpi_value) AS calculated_kpi_value
                FROM lte_fdd_kpi_day
                LEFT JOIN lte_fdd_standard_kpi
                       ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                WHERE timestamp BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                  AND lte_fdd_standard_kpi_id = :standardKpiId
                  AND lte_fdd_kpi_day.rat_id = :ratId
                GROUP BY lte_fdd_standard_kpi.label,
                         lte_fdd_standard_kpi.unit,
                         lte_fdd_standard_kpi.id,
                         lte_fdd_standard_kpi.worst_order
            ) curr
            LEFT JOIN
            (
                SELECT lte_fdd_standard_kpi_id AS id,
                       AVG(kpi_value) AS pre_kpi_value,
                       SUM(numerator_kpi_value) / SUM(denominator_kpi_value) AS pre_calculated_kpi_value
                FROM lte_fdd_kpi_day
                WHERE timestamp BETWEEN (:PreTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :PreTimestamp
                  AND lte_fdd_standard_kpi_id = :standardKpiId
                  AND lte_fdd_kpi_day.rat_id = :ratId
                GROUP BY lte_fdd_standard_kpi_id
            ) pre
            ON curr.id = pre.id;
            """, nativeQuery = true)
    Optional<KpiSnapshotCurrentPre> findLatestAvgKpiSnapshotWithPre(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("PreTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("ratId") Long ratId);


    @Query(value = """
            SELECT curr.label, curr.unit, curr.worst_order, curr.kpi_value, curr.calculated_kpi_value,
                   pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM
            (
                SELECT lte_fdd_standard_kpi.label AS label,
                       lte_fdd_standard_kpi.unit AS unit,
                       lte_fdd_kpi_day.lte_fdd_standard_kpi_id AS id,
                       lte_fdd_standard_kpi.worst_order AS worst_order,
                       SUM(lte_fdd_kpi_day.kpi_value) AS kpi_value,
                       SUM(lte_fdd_kpi_day.numerator_kpi_value) / SUM(lte_fdd_kpi_day.denominator_kpi_value) AS calculated_kpi_value
                FROM lte_fdd_kpi_day
                LEFT JOIN lte_fdd_standard_kpi
                       ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                WHERE "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                  AND lte_fdd_kpi_day.lte_fdd_standard_kpi_id = :standardKpiId
                  AND lte_fdd_kpi_day.rat_id = :ratId
                GROUP BY lte_fdd_standard_kpi.label,
                         lte_fdd_standard_kpi.unit,
                         lte_fdd_kpi_day.lte_fdd_standard_kpi_id,
                         lte_fdd_standard_kpi.worst_order
            ) curr
            LEFT JOIN
            (
                SELECT lte_fdd_kpi_day.lte_fdd_standard_kpi_id AS id,
                       SUM(kpi_value) AS pre_kpi_value,
                       SUM(numerator_kpi_value) / SUM(denominator_kpi_value) AS pre_calculated_kpi_value
                FROM lte_fdd_kpi_day
                WHERE "timestamp" BETWEEN (:PreTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :PreTimestamp
                  AND lte_fdd_kpi_day.lte_fdd_standard_kpi_id = :standardKpiId
                  AND lte_fdd_kpi_day.rat_id = :ratId
                GROUP BY lte_fdd_kpi_day.lte_fdd_standard_kpi_id
            ) pre
            ON curr.id = pre.id;
            """, nativeQuery = true)
    Optional<KpiSnapshotCurrentPre> findLatestSumKpiSnapshotWithPre(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("PreTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("ratId") Long ratId);





    @Query(value = """
            SELECT
                curr.kpi_label,
                curr.unit,
                curr.value,
                pre.previous_value,
                (curr.value - pre.previous_value) AS difference,
                CASE
                    WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
                    WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
                    ELSE 0
                END AS improved
            FROM (
                SELECT
                    lte_fdd_standard_kpi.label AS kpi_label,
                    lte_fdd_standard_kpi.unit AS unit,
                    lte_fdd_standard_kpi.worst_order AS worst_order,
                    COALESCE(
                        CASE
                            WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
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
                  AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                  AND d.id = :districtId
                  AND lte_fdd_kpi_day.rat_id = :ratId
                GROUP BY lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
            ) AS curr
            LEFT JOIN (
                SELECT
                    lte_fdd_standard_kpi.label AS pre_kpi_label,
                    lte_fdd_standard_kpi.unit AS unit,
                    COALESCE(
                        CASE
                            WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
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
                  AND "timestamp" BETWEEN (:preTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :preTimestamp
                  AND d.id = :districtId
                  AND lte_fdd_kpi_day.rat_id = :ratId
                GROUP BY lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit
            ) AS pre
            ON curr.kpi_label = pre.pre_kpi_label;
            
            """, nativeQuery = true)
    Optional<KpiSnapshotDto> findLatestKpiSnapshotByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);


    //  -------------------------- WORST CELLS WITH PREVIOUS - START -------------------------------------------------------

    @Query(value = """
                    WITH agg AS (
                        SELECT
                            cell_name,

                            SUM(numerator_kpi_value)    FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp)     AS curr_num,
                            SUM(denominator_kpi_value)  FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp)     AS curr_den,
                            AVG(kpi_value)              FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp)     AS curr_avg,

                            SUM(numerator_kpi_value)    FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp)   AS pre_num,
                            SUM(denominator_kpi_value)  FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp)   AS pre_den,
                            AVG(kpi_value)              FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp)   AS pre_avg
            
                        FROM lte_fdd_kpi_day
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                          AND rat_id = :ratId
                          AND timestamp BETWEEN :preStart AND :timestamp
                        GROUP BY cell_name
                    ),
            
                    calc AS (
                        SELECT
                            a.cell_name,
                            sk.kpi_name,
                            sk.label AS kpi_label,
                            sk.unit,
                            sk.worst_order,

                            CASE
                                WHEN sk.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den,0)) * 100, a.curr_avg)
                                ELSE COALESCE((a.curr_num / NULLIF(a.curr_den,0)), a.curr_avg)
                            END AS curr_value,

                            CASE
                                WHEN sk.unit = '%' THEN COALESCE((a.pre_num / NULLIF(a.pre_den,0)) * 100, a.pre_avg)
                                ELSE COALESCE((a.pre_num / NULLIF(a.pre_den,0)), a.pre_avg)
                            END AS prev_value
            
                        FROM agg a
                        JOIN lte_fdd_standard_kpi sk ON sk.id = :standardKpiId
                    )
            
                    SELECT
                        cell_name,
                        kpi_name,
                        kpi_label,
                        unit,
                        curr_value AS value,
                        prev_value AS previous_value,
                        (curr_value - prev_value) AS difference,
            
                        CASE
                            WHEN worst_order = 'ASC'  AND (curr_value > prev_value) THEN 1
                            WHEN worst_order = 'DESC' AND (curr_value < prev_value) THEN 1
                            ELSE 0
                        END AS improved
            
                    FROM calc

                    WHERE curr_value IS NOT NULL
                    AND (
                           :excludeZeroes = FALSE\s
                           OR curr_value != 0
                        )
            
                    ORDER BY
                        CASE WHEN worst_order = 'ASC'  THEN curr_value END ASC  NULLS LAST,
                        CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
            
                    LIMIT 25;
            """,
            nativeQuery = true)
    List<WorstCellsDto> findWorstCells(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart , @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes);


    @Query(value = """
            WITH agg AS (
                SELECT
                    cell_name,
                    MAX(timestamp) AS timestamps,
                    rat_id,

                    SUM(numerator_kpi_value)    FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp)     AS curr_num,
                    SUM(denominator_kpi_value)  FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp)     AS curr_den,
                    AVG(kpi_value)              FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp)     AS curr_avg,

                    SUM(numerator_kpi_value)    FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp)   AS pre_num,
                    SUM(denominator_kpi_value)  FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp)   AS pre_den,
                    AVG(kpi_value)              FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp)   AS pre_avg

                FROM lte_fdd_kpi_day
                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                  AND rat_id = :ratId
                  AND timestamp BETWEEN :preStart AND :timestamp
                GROUP BY cell_name, rat_id
            ),

            calc AS (
                SELECT
                    a.timestamps,
                    a.cell_name,
                    sk.id AS standard_kpi_id,
                    --sk.kpi_name,
                    --sk.label AS kpi_label,
                    sk.unit,
                    sk.worst_order,
                    a.rat_id AS rat_id,

                    CASE
                        WHEN sk.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den,0)) * 100, a.curr_avg)
                        ELSE COALESCE((a.curr_num / NULLIF(a.curr_den,0)), a.curr_avg)
                    END AS curr_value,

                    CASE
                        WHEN sk.unit = '%' THEN COALESCE((a.pre_num / NULLIF(a.pre_den,0)) * 100, a.pre_avg)
                        ELSE COALESCE((a.pre_num / NULLIF(a.pre_den,0)), a.pre_avg)
                    END AS prev_value

                FROM agg a
                JOIN lte_fdd_standard_kpi sk ON sk.id = :standardKpiId
            )

            SELECT
                timestamps,
                cell_name,
                standard_kpi_id,
                --kpi_name,
                --kpi_label,
                unit,
                curr_value AS value,
                prev_value AS previous_value,
                (curr_value - prev_value) AS difference,

                CASE
                    WHEN worst_order = 'ASC'  AND (curr_value > prev_value) THEN 1
                    WHEN worst_order = 'DESC' AND (curr_value < prev_value) THEN 1
                    ELSE 0
                END AS improved,
                rat_id

            FROM calc

            WHERE curr_value IS NOT NULL
            AND (
                   :excludeZeroes = FALSE
                   OR curr_value != 0
                )

            ORDER BY
                CASE WHEN worst_order = 'ASC'  THEN curr_value END ASC  NULLS LAST,
                CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST

            LIMIT 25
            """, nativeQuery = true)
    List<DashboardWorstCellDto> findWorstCellsForDashboard(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart , @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes);


    @Query(value = """
            -- Step 1: Filter KPI rows for the given district once
            WITH district_cells AS (
                SELECT l.*
                FROM lte_fdd_kpi_day l
                JOIN district_codes dc ON l.district_code_id = dc.id
                JOIN districts d ON dc.district_id = d.id
                WHERE d.id = :districtId
                  AND l.lte_fdd_standard_kpi_id = :standardKpiId
                  AND l.rat_id = :ratId
                  AND l.timestamp BETWEEN :preStart AND :timestamp -- full range for both periods
            ),
            
            -- Step 2: Aggregate current period
            agg_curr AS (
                SELECT
                    cell_name,
                    SUM(numerator_kpi_value)   FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_num,
                    SUM(denominator_kpi_value) FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_den,
                    AVG(kpi_value)             FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_avg
                FROM district_cells
                GROUP BY cell_name
            ),
            
            -- Step 3: Aggregate previous period
            agg_prev AS (
                SELECT
                    cell_name AS pre_cell_name,
                    SUM(numerator_kpi_value)   FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_num,
                    SUM(denominator_kpi_value) FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_den,
                    AVG(kpi_value)             FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_avg
                FROM district_cells
                GROUP BY cell_name
            ),
            
            -- Step 4: Combine with KPI metadata and calculate values
            calc AS (
                SELECT
                    c.cell_name,
                    sk.kpi_name,
                    sk.label AS kpi_label,
                    sk.unit,
                    sk.worst_order,
                    CASE
                        WHEN sk.unit = '%' THEN COALESCE((c.curr_num / NULLIF(c.curr_den,0)) * 100, c.curr_avg)
                        ELSE COALESCE((c.curr_num / NULLIF(c.curr_den,0)), c.curr_avg)
                    END AS curr_value,
                    CASE
                        WHEN sk.unit = '%' THEN COALESCE((p.pre_num / NULLIF(p.pre_den,0)) * 100, p.pre_avg)
                        ELSE COALESCE((p.pre_num / NULLIF(p.pre_den,0)), p.pre_avg)
                    END AS prev_value
                FROM agg_curr c
                LEFT JOIN agg_prev p ON c.cell_name = p.pre_cell_name
                JOIN lte_fdd_standard_kpi sk ON sk.id = :standardKpiId
            )
            
            -- Step 5: Final selection with NULL-safe ordering
            SELECT
                cell_name,
                kpi_name,
                kpi_label,
                unit,
                curr_value AS value,
                prev_value AS previous_value,
                (curr_value - prev_value) AS difference,
                CASE
                    WHEN worst_order = 'ASC'  AND (curr_value - prev_value) > 0 THEN 1
                    WHEN worst_order = 'DESC' AND (curr_value - prev_value) < 0 THEN 1
                    ELSE 0
                END AS improved
            FROM calc
            WHERE curr_value IS NOT NULL
                AND (
                    :excludeZeroes = FALSE\s
                    OR curr_value != 0
                )
            ORDER BY
                CASE WHEN worst_order = 'ASC' THEN curr_value END ASC NULLS LAST,
                CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
            LIMIT 25;
            """, nativeQuery = true)
    List<WorstCellsDto> findWorstCellsByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart , @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("districtId") Long districtId, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes);


    @Query(value = """
            WITH district_cells AS (
                    SELECT l.*
                    FROM lte_fdd_kpi_day l
                    JOIN district_codes dc ON l.district_code_id = dc.id
                    JOIN districts d ON dc.district_id = d.id
                    WHERE d.id = :districtId
                      AND l.lte_fdd_standard_kpi_id = :standardKpiId
                      AND l.rat_id = :ratId
                      AND l.timestamp BETWEEN :preStart AND :timestamp -- full range for both periods
                ),
    
                -- Step 2: Aggregate current period
                agg_curr AS (
                    SELECT
                        MAX(timestamp) AS timestamps,
                        rat_id,
                        cell_name,
                        SUM(numerator_kpi_value)   FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_num,
                        SUM(denominator_kpi_value) FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_den,
                        AVG(kpi_value)             FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_avg
                    FROM district_cells
                    GROUP BY cell_name, rat_id
                ),
    
                -- Step 3: Aggregate previous period
                agg_prev AS (
                    SELECT
                        cell_name AS pre_cell_name,
                        SUM(numerator_kpi_value)   FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_num,
                        SUM(denominator_kpi_value) FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_den,
                        AVG(kpi_value)             FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_avg
                    FROM district_cells
                    GROUP BY cell_name
                ),
    
                -- Step 4: Combine with KPI metadata and calculate values
                calc AS (
                    SELECT
                        c.timestamps,
                        c.rat_id,
                        c.cell_name,
                        sk.id AS standard_kpi_id,
                        sk.unit,
                        sk.worst_order,
                        CASE
                            WHEN sk.unit = '%' THEN COALESCE((c.curr_num / NULLIF(c.curr_den,0)) * 100, c.curr_avg)
                            ELSE COALESCE((c.curr_num / NULLIF(c.curr_den,0)), c.curr_avg)
                        END AS curr_value,
                        CASE
                            WHEN sk.unit = '%' THEN COALESCE((p.pre_num / NULLIF(p.pre_den,0)) * 100, p.pre_avg)
                            ELSE COALESCE((p.pre_num / NULLIF(p.pre_den,0)), p.pre_avg)
                        END AS prev_value
                    FROM agg_curr c
                    LEFT JOIN agg_prev p ON c.cell_name = p.pre_cell_name
                    JOIN lte_fdd_standard_kpi sk ON sk.id = :standardKpiId
                )
    
                -- Step 5: Final selection with NULL-safe ordering
                SELECT
                    timestamps,
                    cell_name,
                    standard_kpi_id,
                    unit,
                    curr_value AS value,
                    prev_value AS previous_value,
                    (curr_value - prev_value) AS difference,
                    CASE
                        WHEN worst_order = 'ASC'  AND (curr_value - prev_value) > 0 THEN 1
                        WHEN worst_order = 'DESC' AND (curr_value - prev_value) < 0 THEN 1
                        ELSE 0
                    END AS improved,
                    rat_id
                FROM calc
                WHERE curr_value IS NOT NULL
                    AND (
                        :excludeZeroes = FALSE
                        OR curr_value != 0
                    )
                ORDER BY
                    CASE WHEN worst_order = 'ASC' THEN curr_value END ASC NULLS LAST,
                    CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
                LIMIT 10
            """, nativeQuery = true)
    List<DashboardWorstCellDto> findWorstCellsForDashboardByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart , @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("districtId") Long districtId, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes);


    @Query(value = """
            WITH area_cells AS (
                SELECT l.*
                FROM lte_fdd_kpi_day l
                JOIN district_codes dc ON l.district_code_id = dc.id
                JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
                JOIN areas ar ON adcm.area_id = ar.id
                WHERE ar.id = :areaId
                  AND l.lte_fdd_standard_kpi_id = :standardKpiId
                  AND l.rat_id = :ratId
                  AND l.timestamp BETWEEN :preStart AND :timestamp -- full range for both periods
                ),

                -- Step 2: Aggregate current period
                agg_curr AS (
                    SELECT
                        MAX(timestamp) AS timestamps,
                        rat_id,
                        cell_name,
                        SUM(numerator_kpi_value)   FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_num,
                        SUM(denominator_kpi_value) FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_den,
                        AVG(kpi_value)             FILTER (WHERE timestamp BETWEEN :currStart AND :timestamp) AS curr_avg
                    FROM area_cells
                    GROUP BY cell_name, rat_id
                ),

                -- Step 3: Aggregate previous period
                agg_prev AS (
                    SELECT
                        cell_name AS pre_cell_name,
                        SUM(numerator_kpi_value)   FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_num,
                        SUM(denominator_kpi_value) FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_den,
                        AVG(kpi_value)             FILTER (WHERE timestamp BETWEEN :preStart AND :preTimestamp) AS pre_avg
                    FROM area_cells
                    GROUP BY cell_name
                ),

                -- Step 4: Combine with KPI metadata and calculate values
                calc AS (
                    SELECT
                        c.timestamps,
                        c.rat_id,
                        c.cell_name,
                        sk.id AS standard_kpi_id,
                        sk.unit,
                        sk.worst_order,
                        CASE
                            WHEN sk.unit = '%' THEN COALESCE((c.curr_num / NULLIF(c.curr_den,0)) * 100, c.curr_avg)
                            ELSE COALESCE((c.curr_num / NULLIF(c.curr_den,0)), c.curr_avg)
                        END AS curr_value,
                        CASE
                            WHEN sk.unit = '%' THEN COALESCE((p.pre_num / NULLIF(p.pre_den,0)) * 100, p.pre_avg)
                            ELSE COALESCE((p.pre_num / NULLIF(p.pre_den,0)), p.pre_avg)
                        END AS prev_value
                    FROM agg_curr c
                    LEFT JOIN agg_prev p ON c.cell_name = p.pre_cell_name
                    JOIN lte_fdd_standard_kpi sk ON sk.id = :standardKpiId
                )

                -- Step 5: Final selection with NULL-safe ordering
                SELECT
                    timestamps,
                    cell_name,
                    standard_kpi_id,
                    unit,
                    curr_value AS value,
                    prev_value AS previous_value,
                    (curr_value - prev_value) AS difference,
                    CASE
                        WHEN worst_order = 'ASC'  AND (curr_value - prev_value) > 0 THEN 1
                        WHEN worst_order = 'DESC' AND (curr_value - prev_value) < 0 THEN 1
                        ELSE 0
                    END AS improved,
                    rat_id
                FROM calc
                WHERE curr_value IS NOT NULL
                    AND (
                        :excludeZeroes = FALSE
                        OR curr_value != 0
                    )
                ORDER BY
                    CASE WHEN worst_order = 'ASC' THEN curr_value END ASC NULLS LAST,
                    CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
                LIMIT 10
            """, nativeQuery = true)
    List<DashboardWorstCellDto> findWorstCellsForDashboardByArea(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart , @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("areaId") Long areaId, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes);


    // ----------------------------- KPI DATA BY CELL AND KPI ----------------------------------------------------------


    @Query(value = """
            SELECT
                "timestamp",
                cell_name,
                lte_fdd_standard_kpi.label AS label,
                kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
                ON lte_fdd_standard_kpi.id = lte_fdd_kpi_day.lte_fdd_standard_kpi_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
              AND cell_name = :cellName
              AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
              AND lte_fdd_kpi_day.rat_id = :ratId
            
            """, nativeQuery = true)
    List<KpiData> findDataByKpiAndCell(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("cellName") String cellName, @Param("ratId") Long ratId);



    // ----------------------------- KPI TREND DATA BY KPI ----------------------------------------------------------


    @Query(value = """
            WITH params AS (
                SELECT\s
                    :standardKpiId ::bigint AS kpi_id,
                    :ratId ::bigint AS rat_id,
                    :timestamp ::timestamp AS ts,
                    (:timestamp ::date - (:period * INTERVAL '1 day')) AS start_ts
            ),
            agg AS (
                SELECT\s
                    d."timestamp",
                    SUM(d.numerator_kpi_value) AS num,
                    SUM(d.denominator_kpi_value) AS den,
                    AVG(d.kpi_value) AS avg_value
                FROM lte_fdd_kpi_day d
                CROSS JOIN params p
                WHERE d.lte_fdd_standard_kpi_id = p.kpi_id
                  AND d.rat_id = p.rat_id
                  AND d."timestamp" BETWEEN p.start_ts AND p.ts
                GROUP BY d."timestamp"
            )
            SELECT\s
                a."timestamp",
                s.label AS kpi_label,
                CASE
                    WHEN s.unit = '%' THEN COALESCE((a.num / NULLIF(a.den, 0)) * 100, a.avg_value)
                    ELSE COALESCE((a.num / NULLIF(a.den, 0)), a.avg_value)
                END AS kpi_value
            FROM agg a
            JOIN lte_fdd_standard_kpi s
                ON s.id = (SELECT kpi_id FROM params)
            ORDER BY a."timestamp" ASC;
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataAvgByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("ratId") Long ratId);

    @Query(value = """
            WITH params AS (
                SELECT\s
                    :standardKpiId ::bigint AS kpi_id,
                    :ratId ::bigint AS rat_id,
                    :timestamp ::timestamp AS ts,
                    (:timestamp ::date - (:period * INTERVAL '1 day')) AS start_ts
            ),
            agg AS (
                SELECT\s
                    d."timestamp",
                    SUM(d.numerator_kpi_value) AS num,
                    SUM(d.denominator_kpi_value) AS den,
                    SUM(d.kpi_value) AS avg_value
                FROM lte_fdd_kpi_day d
                CROSS JOIN params p
                WHERE d.lte_fdd_standard_kpi_id = p.kpi_id
                  AND d.rat_id = p.rat_id
                  AND d."timestamp" BETWEEN p.start_ts AND p.ts
                GROUP BY d."timestamp"
            )
            SELECT\s
                a."timestamp",
                s.label AS kpi_label,
                CASE
                    WHEN s.unit = '%' THEN COALESCE((a.num / NULLIF(a.den, 0)) * 100, a.avg_value)
                    ELSE COALESCE((a.num / NULLIF(a.den, 0)), a.avg_value)
                END AS kpi_value
            FROM agg a
            JOIN lte_fdd_standard_kpi s
                ON s.id = (SELECT kpi_id FROM params)
            ORDER BY a."timestamp" ASC;
            
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataSumByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("ratId") Long ratId);


    @Query(value = """
            WITH params AS (
                SELECT\s
                    :standardKpiId ::bigint AS kpi_id,
                    :ratId ::bigint AS rat_id,
                    :districtId ::bigint AS district_id,
                    :timestamp ::timestamp AS ts,
                    (:timestamp ::date - (:period * INTERVAL '1 day')) AS start_ts
            ),
            agg AS (
                SELECT\s
                    dday."timestamp",
                    SUM(dday.numerator_kpi_value) AS num,
                    SUM(dday.denominator_kpi_value) AS den,
                    AVG(dday.kpi_value) AS avg_value
                FROM lte_fdd_kpi_day dday
                JOIN district_codes dc\s
                    ON dc.id = dday.district_code_id
                JOIN districts dist
                    ON dist.id = dc.district_id
                CROSS JOIN params p
                WHERE dday.lte_fdd_standard_kpi_id = p.kpi_id
                  AND dday.rat_id = p.rat_id
                  AND dist.id = p.district_id
                  AND dday."timestamp" BETWEEN p.start_ts AND p.ts
                GROUP BY dday."timestamp"
            )
            SELECT\s
                a."timestamp",
                sk.label AS kpi_label,
                CASE
                    WHEN sk.unit = '%' THEN COALESCE((a.num / NULLIF(a.den, 0)) * 100, a.avg_value)
                    ELSE COALESCE((a.num / NULLIF(a.den, 0)), a.avg_value)
                END AS kpi_value
            FROM agg a
            JOIN lte_fdd_standard_kpi sk\s
                 ON sk.id = (SELECT kpi_id FROM params)
            ORDER BY a."timestamp" ASC;
            
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataAvgByKpiAndDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);

    @Query(value = """
            WITH params AS (
                SELECT\s
                    :standardKpiId ::bigint AS kpi_id,
                    :ratId ::bigint AS rat_id,
                    :districtId ::bigint AS district_id,
                    :timestamp ::timestamp AS ts,
                    (:timestamp ::date - (:period * INTERVAL '1 day')) AS start_ts
            ),
            agg AS (
                SELECT\s
                    dday."timestamp",
                    SUM(dday.numerator_kpi_value) AS num,
                    SUM(dday.denominator_kpi_value) AS den,
                    SUM(dday.kpi_value) AS avg_value
                FROM lte_fdd_kpi_day dday
                JOIN district_codes dc\s
                    ON dc.id = dday.district_code_id
                JOIN districts dist
                    ON dist.id = dc.district_id
                CROSS JOIN params p
                WHERE dday.lte_fdd_standard_kpi_id = p.kpi_id
                  AND dday.rat_id = p.rat_id
                  AND dist.id = p.district_id
                  AND dday."timestamp" BETWEEN p.start_ts AND p.ts
                GROUP BY dday."timestamp"
            )
            SELECT\s
                a."timestamp",
                sk.label AS kpi_label,
                CASE
                    WHEN sk.unit = '%' THEN COALESCE((a.num / NULLIF(a.den, 0)) * 100, a.avg_value)
                    ELSE COALESCE((a.num / NULLIF(a.den, 0)), a.avg_value)
                END AS kpi_value
            FROM agg a
            JOIN lte_fdd_standard_kpi sk\s
                 ON sk.id = (SELECT kpi_id FROM params)
            ORDER BY a."timestamp" ASC;
            
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataSumByKpiAndDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);






    @Query(value = """
            SELECT * FROM lte_fdd_kpi_day
            WHERE district_code_id IS NULL AND rat_id = :ratId
            """, nativeQuery = true)
    List<KpiDay> findKpiWithoutDistrict(@Param("ratId") Long ratId);



    //============= Cell Name  =======================

    @Query(value = """
            SELECT DISTINCT ON (l.cell_name)
                   l.cell_name,
                   r.name  AS rat_name,
                   r.label AS rat_label
            FROM lte_fdd_kpi_day l
            LEFT JOIN rat r ON r.id = l.rat_id
            ORDER BY l.cell_name ASC
            """, nativeQuery = true)
    List<CellNameDto> getAllCellNames();
}
