package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.KpiDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KpiDayRepository extends JpaRepository<KpiDay, Long> {

    @Query(value = "SELECT DISTINCT timestamp FROM kpi_values WHERE kpi_values.rat_id = :ratId AND kpi_values.granularity_id = :granularityId ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate(@Param("ratId") Long ratId, @Param("granularityId") Long granularityId);

    @Query(value = """
            SELECT DISTINCT timestamp FROM kpi_values
            ORDER BY timestamp DESC LIMIT 1;
            """, nativeQuery = true)
    LocalDateTime getLatestDate();

    @Query(value = """
            SELECT curr.label, curr.unit, curr.worst_order, curr.kpi_value, curr.calculated_kpi_value,
                   pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM
            (
                SELECT standard_kpi.label AS label,
                       standard_kpi.unit AS unit,
                       standard_kpi.id AS id,
                       standard_kpi.worst_order AS worst_order,
                       AVG(kpi_value) AS kpi_value,
                       SUM(numerator_kpi_value) / SUM(denominator_kpi_value) AS calculated_kpi_value
                FROM kpi_values
                LEFT JOIN standard_kpi
                       ON kpi_values.standard_kpi_id = standard_kpi.id
                WHERE timestamp BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                  AND standard_kpi_id = :standardKpiId
                  AND kpi_values.rat_id = :ratId
                  AND kpi_values.granularity_id = :granularityId
                GROUP BY standard_kpi.label,
                         standard_kpi.unit,
                         standard_kpi.id,
                         standard_kpi.worst_order
            ) curr
            LEFT JOIN
            (
                SELECT standard_kpi_id AS id,
                       AVG(kpi_value) AS pre_kpi_value,
                       SUM(numerator_kpi_value) / SUM(denominator_kpi_value) AS pre_calculated_kpi_value
                FROM kpi_values
                WHERE timestamp BETWEEN (:PreTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :PreTimestamp
                  AND standard_kpi_id = :standardKpiId
                  AND kpi_values.rat_id = :ratId
                  AND kpi_values.granularity_id = :granularityId
                GROUP BY standard_kpi_id
            ) pre
            ON curr.id = pre.id;
            """, nativeQuery = true)
    Optional<KpiSnapshotCurrentPre> findLatestAvgKpiSnapshotWithPre(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("PreTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


    @Query(value = """
            SELECT curr.label, curr.unit, curr.worst_order, curr.kpi_value, curr.calculated_kpi_value,
                   pre.pre_kpi_value, pre.pre_calculated_kpi_value
            FROM
            (
                SELECT standard_kpi.label AS label,
                       standard_kpi.unit AS unit,
                       kpi_values.standard_kpi_id AS id,
                       standard_kpi.worst_order AS worst_order,
                       SUM(kpi_values.kpi_value) AS kpi_value,
                       SUM(kpi_values.numerator_kpi_value) / SUM(kpi_values.denominator_kpi_value) AS calculated_kpi_value
                FROM kpi_values
                LEFT JOIN standard_kpi
                       ON kpi_values.standard_kpi_id = standard_kpi.id
                WHERE "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                  AND kpi_values.standard_kpi_id = :standardKpiId
                  AND kpi_values.rat_id = :ratId
                  AND kpi_values.granularity_id = :granularityId
                GROUP BY standard_kpi.label,
                         standard_kpi.unit,
                         kpi_values.standard_kpi_id,
                         standard_kpi.worst_order
            ) curr
            LEFT JOIN
            (
                SELECT kpi_values.standard_kpi_id AS id,
                       SUM(kpi_value) AS pre_kpi_value,
                       SUM(numerator_kpi_value) / SUM(denominator_kpi_value) AS pre_calculated_kpi_value
                FROM kpi_values
                WHERE "timestamp" BETWEEN (:PreTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :PreTimestamp
                  AND kpi_values.standard_kpi_id = :standardKpiId
                  AND kpi_values.rat_id = :ratId
                  AND kpi_values.granularity_id = :granularityId
                GROUP BY kpi_values.standard_kpi_id
            ) pre
            ON curr.id = pre.id;
            """, nativeQuery = true)
    Optional<KpiSnapshotCurrentPre> findLatestSumKpiSnapshotWithPre(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("PreTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


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
                    standard_kpi.label AS kpi_label,
                    standard_kpi.unit AS unit,
                    standard_kpi.worst_order AS worst_order,
                    COALESCE(
                        CASE
                            WHEN standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                            ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                        END,
                        AVG(kpi_value)
                    ) AS value
                FROM kpi_values
                LEFT JOIN standard_kpi
                    ON kpi_values.standard_kpi_id = standard_kpi.id
                JOIN district_codes dc
                    ON dc.id = district_code_id
                JOIN districts d
                    ON d.id = dc.district_id
                WHERE standard_kpi_id = :standardKpiId
                  AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                  AND d.id = :districtId
                  AND kpi_values.rat_id = :ratId
                  AND kpi_values.granularity_id = :granularityId
                GROUP BY standard_kpi.label, standard_kpi.unit, standard_kpi.worst_order
            ) AS curr
            LEFT JOIN (
                SELECT
                    standard_kpi.label AS pre_kpi_label,
                    standard_kpi.unit AS unit,
                    COALESCE(
                        CASE
                            WHEN standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))*100
                            ELSE (SUM(numerator_kpi_value)/SUM(denominator_kpi_value))
                        END,
                        AVG(kpi_value)
                    ) AS previous_value
                FROM kpi_values
                LEFT JOIN standard_kpi
                    ON kpi_values.standard_kpi_id = standard_kpi.id
                JOIN district_codes dc
                    ON dc.id = district_code_id
                JOIN districts d
                    ON d.id = dc.district_id
                WHERE standard_kpi_id = :standardKpiId
                  AND "timestamp" BETWEEN (:preTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :preTimestamp
                  AND d.id = :districtId
                  AND kpi_values.rat_id = :ratId
                  AND kpi_values.granularity_id = :granularityId
                GROUP BY standard_kpi.label, standard_kpi.unit
            ) AS pre
            ON curr.kpi_label = pre.pre_kpi_label;
            
            """, nativeQuery = true)
    Optional<KpiSnapshotDto> findLatestKpiSnapshotByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);

    @Query(value = """
            WITH params AS (
                SELECT
                    :currStart ::timestamp AS curr_start,
                    :currEnd ::timestamp AS curr_end,
                    :prevStart ::timestamp AS prev_start,
                    :prevEnd ::timestamp AS prev_end,
                    :standardKpiId  ::bigint AS kpi_id,
                    :areaId ::bigint AS area_id,
                    :ratId  ::bigint AS rat_id,
                    :granularityId  ::bigint AS granularity_id
            ),
            agg AS (
                SELECT
                    sk.label,
                    sk.unit,
                    sk.worst_order,
            
                    --current
                    SUM(d.numerator_kpi_value)
                        FILTER (WHERE d.timestamp >= p.curr_start AND d.timestamp <= p.curr_end) AS curr_num,
                    SUM(d.denominator_kpi_value)
                        FILTER (WHERE d.timestamp >= p.curr_start AND d.timestamp <= p.curr_end) AS curr_den,
                    AVG(d.kpi_value)
                        FILTER (WHERE d.timestamp >= p.curr_start AND d.timestamp <= p.curr_end) AS curr_avg,
            
                    --previous
                    SUM(d.numerator_kpi_value)
                        FILTER (WHERE d.timestamp >= p.prev_start AND d.timestamp <= p.prev_end) AS prev_num,
                    SUM(d.denominator_kpi_value)
                        FILTER (WHERE d.timestamp >= p.prev_start AND d.timestamp <= p.prev_end) AS prev_den,
                    AVG(d.kpi_value)
                        FILTER (WHERE d.timestamp >= p.prev_start AND d.timestamp <= p.prev_end) AS prev_avg
            
                FROM kpi_values d
                JOIN params p ON TRUE
                JOIN standard_kpi sk ON sk.id = p.kpi_id
                JOIN district_codes dc ON dc.id = d.district_code_id
                JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
                JOIN areas ar ON ar.id = adcm.area_id
            
                WHERE d.standard_kpi_id = p.kpi_id
                  AND ar.id = p.area_id
                  AND d.rat_id = p.rat_id
                  AND d.granularity_id = p.granularity_id
                  AND d.timestamp >= p.prev_start
                  AND d.timestamp <=  p.curr_end
            
                GROUP BY sk.label, sk.unit, sk.worst_order
            ),
            calc AS (
                SELECT
                    label AS kpi_label,
                    unit,
                    worst_order,
            
                    CASE
                        WHEN unit = '%' THEN COALESCE((curr_num / NULLIF(curr_den,0)) * 100, curr_avg)
                        ELSE COALESCE((curr_num / NULLIF(curr_den,0)), curr_avg)
                    END AS value,
            
                    CASE
                        WHEN unit = '%' THEN COALESCE((prev_num / NULLIF(prev_den,0)) * 100, prev_avg)
                        ELSE COALESCE((prev_num / NULLIF(prev_den,0)), prev_avg)
                    END AS previous_value
                FROM agg
            )
            SELECT
                kpi_label,
                unit,
                value,
                previous_value,
                (value - previous_value) AS difference,
            
                CASE
                    WHEN worst_order = 'ASC'  AND value > previous_value THEN 1
                    WHEN worst_order = 'DESC' AND value < previous_value THEN 1
                    ELSE 0
                END AS improved
            
            FROM calc
            """, nativeQuery = true)
    Optional<KpiSnapshotDto> findKpiSnapshotByArea(@Param("standardKpiId") Long standardKpiId, @Param("currStart") LocalDateTime currStart, @Param("currEnd") LocalDateTime currEnd, @Param("prevStart") LocalDateTime prevStart, @Param("prevEnd") LocalDateTime prevEnd, @Param("areaId") Long areaId, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


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
            
                FROM kpi_values
                WHERE standard_kpi_id = :standardKpiId
                  AND rat_id = :ratId
                  AND timestamp BETWEEN :preStart AND :timestamp
                  AND granularity_id = :granularityId
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
                JOIN standard_kpi sk ON sk.id = :standardKpiId
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
                   :excludeZeroes = FALSE
                   OR curr_value != 0
                )
            
            ORDER BY
                CASE WHEN worst_order = 'ASC'  THEN curr_value END ASC  NULLS LAST,
                CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
            
            LIMIT 25;
            """,
            nativeQuery = true)
    List<WorstCellsDto> findWorstCells(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes, @Param("granularityId") Long granularityId);


//    @Query(value = """
//            WITH params AS (
//                 SELECT
//                    :currStart ::timestamp AS curr_start,
//                    :currEnd  ::timestamp AS curr_end,
//                    :prevStart ::timestamp AS prev_start,
//                    :prevEnd ::timestamp AS prev_end,
//                    :standardKpiId  ::bigint AS kpi_id,
//                    :areaId ::bigint AS area_id,
//                    :ratId  ::bigint AS rat_id,
//                    :granularityId  ::bigint AS granularity_id,
//                    :excludeZeroes ::boolean AS exclude_zeroes
//                ),
//                -- Step 1: Filter KPI rows once
//                district_cells AS (
//                    SELECT l.*
//                    FROM kpi_values l
//                    JOIN district_codes dc ON l.district_code_id = dc.id
//                    JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
//                    JOIN areas ar ON adcm.area_id = ar.id
//                    CROSS JOIN params p
//                    WHERE ar.id = p.area_id
//                        AND l.standard_kpi_id = p.kpi_id
//                        AND l.rat_id = p.rat_id
//                        AND l.granularity_id = p.granularity_id
//                        AND l.timestamp BETWEEN p.prev_start AND p.curr_end
//                    ),
//                -- Step 2: Aggregate current period
//                agg_curr AS (
//                    SELECT
//                        cell_name,
//                        SUM(numerator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_num,
//                        SUM(denominator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_den,
//                        AVG(kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_avg
//                    FROM district_cells
//                    CROSS JOIN params p
//                    GROUP BY cell_name
//                ),
//                -- Step 3: Aggregate previous period
//                agg_prev AS (
//                    SELECT
//                        cell_name AS pre_cell_name,
//                        SUM(numerator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.prev_start AND p.prev_end) AS pre_num,
//                        SUM(denominator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.prev_start AND p.prev_end) AS pre_den,
//                        AVG(kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.prev_start AND p.prev_end) AS pre_avg
//                    FROM district_cells
//                    CROSS JOIN params p
//                    GROUP BY cell_name
//                ),
//                -- Step 4: Combine with KPI metadata
//                calc AS (
//                 SELECT
//                     c.cell_name,
//                     sk.kpi_name,
//                     sk.label AS kpi_label,
//                     sk.unit,
//                     sk.worst_order,
//                     CASE
//                         WHEN sk.unit = '%' THEN COALESCE((c.curr_num / NULLIF(c.curr_den,0)) * 100, c.curr_avg)
//                         ELSE COALESCE((c.curr_num / NULLIF(c.curr_den,0)), c.curr_avg)
//                     END AS curr_value,
//                     CASE
//                         WHEN sk.unit = '%' THEN COALESCE((p.pre_num / NULLIF(p.pre_den,0)) * 100, p.pre_avg)
//                         ELSE COALESCE((p.pre_num / NULLIF(p.pre_den,0)), p.pre_avg)
//                     END AS prev_value
//                 FROM agg_curr c
//                 LEFT JOIN agg_prev p ON c.cell_name = p.pre_cell_name
//                 JOIN standard_kpi sk
//                    ON sk.id = (SELECT kpi_id FROM params)  -- Correctly reference params
//                )
//                SELECT
//                    cell_name,
//                    kpi_name,
//                    kpi_label,
//                    unit,
//                    curr_value AS value,
//                    prev_value AS previous_value,
//                    (curr_value - prev_value) AS difference,
//                        CASE
//                            WHEN worst_order = 'ASC'  AND (curr_value - prev_value) > 0 THEN 1
//                            WHEN worst_order = 'DESC' AND (curr_value - prev_value) < 0 THEN 1
//                            ELSE 0
//                        END AS improved
//                    FROM calc
//                    CROSS JOIN params p
//                    WHERE curr_value IS NOT NULL
//                        AND (p.exclude_zeroes = FALSE OR curr_value <> 0)
//                    ORDER BY
//                        CASE WHEN worst_order = 'ASC'  THEN curr_value END ASC NULLS LAST,
//                        CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
//                    LIMIT :limit;
//            """, nativeQuery = true)
//    List<WorstCellsDto> findWorstCellsByArea(
//            @Param("standardKpiId") Long standardKpiId,
//            @Param("currStart") LocalDateTime currStart,
//            @Param("currEnd") LocalDateTime currEnd,
//            @Param("prevStart") LocalDateTime prevStart,
//            @Param("prevEnd") LocalDateTime prevEnd,
//            @Param("limit") int limit,
//            @Param("areaId") Long areaId,
//            @Param("ratId") Long ratId,
//            @Param("excludeZeroes") boolean excludeZeroes,
//            @Param("granularityId") Long granularityId
//    );


    @Query(value = """
        WITH params AS (
            SELECT
                :currStart     ::timestamp AS curr_start,
                :currEnd       ::timestamp AS curr_end,
                :prevStart     ::timestamp AS prev_start,
                :prevEnd       ::timestamp AS prev_end,
                :standardKpiId ::bigint    AS kpi_id,
                :areaId        ::bigint    AS area_id,
                :ratId         ::bigint    AS rat_id,
                :granularityId ::bigint    AS granularity_id,
                :excludeZeroes ::boolean   AS exclude_zeroes
        ),
        area_codes AS MATERIALIZED (
            SELECT ARRAY_AGG(district_code_id) AS ids
            FROM area_district_code_mapping
            WHERE area_id = (SELECT area_id FROM params)
        ),
        kpi_meta AS MATERIALIZED (
            SELECT kpi_name, label, unit, worst_order, threshold
            FROM standard_kpi
            WHERE id = (SELECT kpi_id FROM params)
        ),
        agg AS MATERIALIZED (
            SELECT
                l.cell_name,
                SUM(l.numerator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_num,
                SUM(l.denominator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_den,
                AVG(l.kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_avg,
                SUM(l.numerator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.prev_start AND p.prev_end) AS prev_num,
                SUM(l.denominator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.prev_start AND p.prev_end) AS prev_den,
                AVG(l.kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.prev_start AND p.prev_end) AS prev_avg
            FROM kpi_values l, area_codes ac, params p
            WHERE l.district_code_id = ANY(ac.ids)
                AND l.standard_kpi_id = p.kpi_id
                AND l.rat_id          = p.rat_id
                AND l.granularity_id  = p.granularity_id
                AND l.timestamp BETWEEN p.prev_start AND p.curr_end
            GROUP BY l.cell_name
        )
        SELECT
            a.cell_name     AS cell_name,
            km.kpi_name     AS kpi_name,
            km.label        AS kpi_label,
            km.unit         AS unit,
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
            END             AS value,
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                ELSE                    COALESCE((a.prev_num / NULLIF(a.prev_den, 0)),        a.prev_avg)
            END             AS previous_value,
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
            END -
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                ELSE                    COALESCE((a.prev_num / NULLIF(a.prev_den, 0)),        a.prev_avg)
            END             AS difference,
            CASE
                WHEN km.worst_order = 'ASC'  AND (
                    CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                         ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
                    -
                    CASE WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                         ELSE COALESCE((a.prev_num / NULLIF(a.prev_den, 0)), a.prev_avg) END
                ) > 0 THEN 1
                WHEN km.worst_order = 'DESC' AND (
                    CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                         ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
                    -
                    CASE WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                         ELSE COALESCE((a.prev_num / NULLIF(a.prev_den, 0)), a.prev_avg) END
                ) < 0 THEN 1
                ELSE 0
            END             AS improved
        FROM agg a
        CROSS JOIN kpi_meta km
        CROSS JOIN params p
        WHERE
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
            END IS NOT NULL
            AND (p.exclude_zeroes = FALSE OR
                CASE
                    WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                    ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
                END <> 0)
        ORDER BY
            CASE WHEN km.worst_order = 'ASC'  THEN
                CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                     ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
            END ASC  NULLS LAST,
            CASE WHEN km.worst_order = 'DESC' THEN
                CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                     ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
            END DESC NULLS LAST
        LIMIT :limit;
        """, nativeQuery = true)
    List<WorstCellsProjection> findWorstCellsByArea(
            @Param("standardKpiId") Long standardKpiId,
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("prevStart") LocalDateTime prevStart,
            @Param("prevEnd") LocalDateTime prevEnd,
            @Param("limit") int limit,
            @Param("areaId") Long areaId,
            @Param("ratId") Long ratId,
            @Param("excludeZeroes") boolean excludeZeroes,
            @Param("granularityId") Long granularityId
    );


    @Query(value = """
        WITH kpi_meta AS MATERIALIZED (
            SELECT unit, worst_order, threshold
            FROM standard_kpi
            WHERE id = :standardKpiId
        ),
        streak_raw AS (
            SELECT
                l.cell_name,
                ROW_NUMBER() OVER (PARTITION BY l.cell_name ORDER BY l.timestamp DESC) AS rn,
                CASE
                    WHEN km.worst_order = 'ASC'  AND
                         COALESCE(l.numerator_kpi_value / NULLIF(l.denominator_kpi_value, 0)
                             * CASE WHEN km.unit = '%' THEN 100 ELSE 1 END,
                             l.kpi_value) < km.threshold THEN 1
                    WHEN km.worst_order = 'DESC' AND
                         COALESCE(l.numerator_kpi_value / NULLIF(l.denominator_kpi_value, 0)
                             * CASE WHEN km.unit = '%' THEN 100 ELSE 1 END,
                             l.kpi_value) > km.threshold THEN 1
                    ELSE 0
                END AS is_bad
            FROM kpi_values l
            CROSS JOIN kpi_meta km
            WHERE l.cell_name      = ANY(:cellNames ::varchar[])
                AND l.standard_kpi_id = :standardKpiId
                AND l.rat_id          = :ratId
                AND l.granularity_id  = :granularityId
                AND l.timestamp BETWEEN :streakStart AND :currEnd
        )
        SELECT
            cell_name                                            AS cell_name,
            LEAST(
                COALESCE(MIN(rn) FILTER (WHERE is_bad = 0) - 1, COUNT(*)),
                31
            )::int AS consecutive_bad_days
        FROM streak_raw
        GROUP BY cell_name;
        """, nativeQuery = true)
    List<CellStreakProjection> findStreaksForCells(
            @Param("standardKpiId") Long standardKpiId,
            @Param("streakStart") LocalDateTime streakStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("cellNames") String[] cellNames,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId
    );


    @Query(value = """
        WITH params AS (
            SELECT
                :currStart     ::timestamp AS curr_start,
                :currEnd       ::timestamp AS curr_end,
                :prevStart     ::timestamp AS prev_start,
                :prevEnd       ::timestamp AS prev_end,
                :standardKpiId ::bigint    AS kpi_id,
                :areaId        ::bigint    AS area_id,
                :ratId         ::bigint    AS rat_id,
                :granularityId ::bigint    AS granularity_id,
                :excludeZeroes ::boolean   AS exclude_zeroes,
                :bandId        ::bigint    AS band_id
        ),
        area_codes AS MATERIALIZED (
            SELECT ARRAY_AGG(adcm.district_code_id) AS ids
            FROM area_district_code_mapping adcm
            WHERE adcm.area_id = (SELECT area_id FROM params)
        ),
        band_cells AS MATERIALIZED (
            SELECT c.cell_name
            FROM cells c
            WHERE c.band_id = (SELECT band_id FROM params)
        ),
        kpi_meta AS MATERIALIZED (
            SELECT kpi_name, label, unit, worst_order, threshold
            FROM standard_kpi
            WHERE id = (SELECT kpi_id FROM params)
        ),
        agg AS MATERIALIZED (
            SELECT
                l.cell_name,
                SUM(l.numerator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_num,
                SUM(l.denominator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_den,
                AVG(l.kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_avg,
                SUM(l.numerator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.prev_start AND p.prev_end) AS prev_num,
                SUM(l.denominator_kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.prev_start AND p.prev_end) AS prev_den,
                AVG(l.kpi_value)
                    FILTER (WHERE l.timestamp BETWEEN p.prev_start AND p.prev_end) AS prev_avg
            FROM kpi_values l, area_codes ac, band_cells bc, params p
            WHERE l.district_code_id  = ANY(ac.ids)
                AND l.cell_name       = bc.cell_name
                AND l.standard_kpi_id = p.kpi_id
                AND l.rat_id          = p.rat_id
                AND l.granularity_id  = p.granularity_id
                AND l.timestamp BETWEEN p.prev_start AND p.curr_end
            GROUP BY l.cell_name
        )
        SELECT
            a.cell_name                                                         AS cell_name,
            km.kpi_name                                                         AS kpi_name,
            km.label                                                            AS kpi_label,
            km.unit                                                             AS unit,
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
            END                                                                 AS value,
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                ELSE                    COALESCE((a.prev_num / NULLIF(a.prev_den, 0)),        a.prev_avg)
            END                                                                 AS previous_value,
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
            END -
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                ELSE                    COALESCE((a.prev_num / NULLIF(a.prev_den, 0)),        a.prev_avg)
            END                                                                 AS difference,
            CASE
                WHEN km.worst_order = 'ASC'  AND (
                    CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                         ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
                    -
                    CASE WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                         ELSE COALESCE((a.prev_num / NULLIF(a.prev_den, 0)), a.prev_avg) END
                ) > 0 THEN 1
                WHEN km.worst_order = 'DESC' AND (
                    CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                         ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
                    -
                    CASE WHEN km.unit = '%' THEN COALESCE((a.prev_num / NULLIF(a.prev_den, 0)) * 100, a.prev_avg)
                         ELSE COALESCE((a.prev_num / NULLIF(a.prev_den, 0)), a.prev_avg) END
                ) < 0 THEN 1
                ELSE 0
            END                                                                 AS improved
        FROM agg a
        CROSS JOIN kpi_meta km
        CROSS JOIN params p
        WHERE
            CASE
                WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
            END IS NOT NULL
            AND (p.exclude_zeroes = FALSE OR
                CASE
                    WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                    ELSE                    COALESCE((a.curr_num / NULLIF(a.curr_den, 0)),        a.curr_avg)
                END <> 0)
        ORDER BY
            CASE WHEN km.worst_order = 'ASC'  THEN
                CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                     ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
            END ASC  NULLS LAST,
            CASE WHEN km.worst_order = 'DESC' THEN
                CASE WHEN km.unit = '%' THEN COALESCE((a.curr_num / NULLIF(a.curr_den, 0)) * 100, a.curr_avg)
                     ELSE COALESCE((a.curr_num / NULLIF(a.curr_den, 0)), a.curr_avg) END
            END DESC NULLS LAST
        LIMIT :limit;
        """, nativeQuery = true)
    List<WorstCellsProjection> findWorstCellsByAreaAndBand(
            @Param("standardKpiId") Long standardKpiId,
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("prevStart") LocalDateTime prevStart,
            @Param("prevEnd") LocalDateTime prevEnd,
            @Param("limit") int limit,
            @Param("areaId") Long areaId,
            @Param("ratId") Long ratId,
            @Param("excludeZeroes") boolean excludeZeroes,
            @Param("granularityId") Long granularityId,
            @Param("bandId") Long bandId
    );


//    @Query(value = """
//            WITH params AS (
//                 SELECT
//                    :currStart ::timestamp AS curr_start,
//                    :currEnd  ::timestamp AS curr_end,
//                    :prevStart ::timestamp AS prev_start,
//                    :prevEnd ::timestamp AS prev_end,
//                    :standardKpiId  ::bigint AS kpi_id,
//                    :areaId ::bigint AS area_id,
//                    :ratId  ::bigint AS rat_id,
//                    :granularityId  ::bigint AS granularity_id,
//                    :excludeZeroes ::boolean AS exclude_zeroes,
//                    :bandId ::bigint AS band_id
//                ),
//                -- Step 1: Filter KPI rows once
//                district_cells AS (
//                    SELECT l.*
//                    FROM kpi_values l
//                    JOIN district_codes dc ON l.district_code_id = dc.id
//                    JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
//                    JOIN areas ar ON adcm.area_id = ar.id
//                    JOIN cells c ON l.cell_name = c.cell_name
//                    JOIN bands b ON b.id = c.band_id
//                    CROSS JOIN params p
//                    WHERE ar.id = p.area_id
//                        AND l.standard_kpi_id = p.kpi_id
//                        AND l.rat_id = p.rat_id
//                        AND l.granularity_id = p.granularity_id
//                        AND l.timestamp BETWEEN p.prev_start AND p.curr_end
//                        AND b.id = p.band_id
//                    ),
//                -- Step 2: Aggregate current period
//                agg_curr AS (
//                    SELECT
//                        cell_name,
//                        SUM(numerator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_num,
//                        SUM(denominator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_den,
//                        AVG(kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.curr_start AND p.curr_end) AS curr_avg
//                    FROM district_cells
//                    CROSS JOIN params p
//                    GROUP BY cell_name
//                ),
//                -- Step 3: Aggregate previous period
//                agg_prev AS (
//                    SELECT
//                        cell_name AS pre_cell_name,
//                        SUM(numerator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.prev_start AND p.prev_end) AS pre_num,
//                        SUM(denominator_kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.prev_start AND p.prev_end) AS pre_den,
//                        AVG(kpi_value)
//                            FILTER (WHERE timestamp BETWEEN p.prev_start AND p.prev_end) AS pre_avg
//                    FROM district_cells
//                    CROSS JOIN params p
//                    GROUP BY cell_name
//                ),
//                -- Step 4: Combine with KPI metadata
//                calc AS (
//                 SELECT
//                     c.cell_name,
//                     sk.kpi_name,
//                     sk.label AS kpi_label,
//                     sk.unit,
//                     sk.worst_order,
//                     CASE
//                         WHEN sk.unit = '%' THEN COALESCE((c.curr_num / NULLIF(c.curr_den,0)) * 100, c.curr_avg)
//                         ELSE COALESCE((c.curr_num / NULLIF(c.curr_den,0)), c.curr_avg)
//                     END AS curr_value,
//                     CASE
//                         WHEN sk.unit = '%' THEN COALESCE((p.pre_num / NULLIF(p.pre_den,0)) * 100, p.pre_avg)
//                         ELSE COALESCE((p.pre_num / NULLIF(p.pre_den,0)), p.pre_avg)
//                     END AS prev_value
//                 FROM agg_curr c
//                 LEFT JOIN agg_prev p ON c.cell_name = p.pre_cell_name
//                 JOIN standard_kpi sk
//                    ON sk.id = (SELECT kpi_id FROM params)  -- Correctly reference params
//                )
//                SELECT
//                    cell_name,
//                    kpi_name,
//                    kpi_label,
//                    unit,
//                    curr_value AS value,
//                    prev_value AS previous_value,
//                    (curr_value - prev_value) AS difference,
//                        CASE
//                            WHEN worst_order = 'ASC'  AND (curr_value - prev_value) > 0 THEN 1
//                            WHEN worst_order = 'DESC' AND (curr_value - prev_value) < 0 THEN 1
//                            ELSE 0
//                        END AS improved
//                    FROM calc
//                    CROSS JOIN params p
//                    WHERE curr_value IS NOT NULL
//                        AND (p.exclude_zeroes = FALSE OR curr_value <> 0)
//                    ORDER BY
//                        CASE WHEN worst_order = 'ASC'  THEN curr_value END ASC NULLS LAST,
//                        CASE WHEN worst_order = 'DESC' THEN curr_value END DESC NULLS LAST
//                    LIMIT :limit;
//            """, nativeQuery = true)
//    List<WorstCellsDto> findWorstCellsByAreaAndBand(
//            @Param("standardKpiId") Long standardKpiId,
//            @Param("currStart") LocalDateTime currStart,
//            @Param("currEnd") LocalDateTime currEnd,
//            @Param("prevStart") LocalDateTime prevStart,
//            @Param("prevEnd") LocalDateTime prevEnd,
//            @Param("limit") int limit,
//            @Param("areaId") Long areaId,
//            @Param("ratId") Long ratId,
//            @Param("excludeZeroes") boolean excludeZeroes,
//            @Param("granularityId") Long granularityId,
//            @Param("bandId") Long bandId
//    );


    @Query(value = """
            WITH area_cells AS (
                SELECT l.*
                FROM kpi_values l
                JOIN district_codes dc ON l.district_code_id = dc.id
                JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
                JOIN areas ar ON adcm.area_id = ar.id
                WHERE ar.id = :areaId
                  AND l.standard_kpi_id = :standardKpiId
                  AND l.rat_id = :ratId
                  AND l.timestamp BETWEEN :preStart AND :timestamp -- full range for both periods
                  AND l.granularity_id = :granularityId
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
                    JOIN standard_kpi sk ON sk.id = :standardKpiId
                )
            
                -- Step 5: Final selection with NULL-safe ordering
                SELECT
                    date_trunc('day', timestamps) AS timestamps,
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
                    rat_id,
                    :excludeZeroes AS exclude_zeroes,
                    :granularityId AS granularity_id
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
    List<WorstCellSaveDto> findWorstCellsForDashboardByArea(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("currStart") LocalDateTime currentStart, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("preStart") LocalDateTime previousStart, @Param("areaId") Long areaId, @Param("ratId") Long ratId, @Param("excludeZeroes") boolean excludeZeroes, @Param("granularityId") Long granularityId);


    // ----------------------------- KPI DATA BY CELL AND KPI ----------------------------------------------------------


    @Query(value = """
            SELECT
                "timestamp",
                cell_name,
                standard_kpi.label AS label,
                kpi_value
            FROM kpi_values
            LEFT JOIN standard_kpi
                ON standard_kpi.id = kpi_values.standard_kpi_id
            WHERE standard_kpi_id = :standardKpiId
              AND cell_name = :cellName
              AND "timestamp" BETWEEN :startTimestamp AND :timestamp
              AND kpi_values.rat_id = :ratId
              AND kpi_values.granularity_id = :granularityId
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
            FROM kpi_values
            LEFT JOIN standard_kpi
                ON standard_kpi.id = kpi_values.standard_kpi_id
            WHERE standard_kpi_id = :standardKpiId
              AND cell_name = :cellName
              AND "timestamp" BETWEEN :startTimestamp AND :timestamp
              AND kpi_values.rat_id = :ratId
              AND kpi_values.granularity_id = :granularityId
            ORDER BY timestamp ASC
            """, nativeQuery = true)
    List<KpiDataWithOperandsDto> findDataByKpiAndCellWithOperands(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("cellName") String cellName, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);

    // ----------------------------- KPI TREND DATA BY KPI ----------------------------------------------------------


    @Query(value = """
             SELECT
                 date_trunc('day', d."timestamp") AS "timestamp",
                 skpi.label AS kpi_label,
                 CASE
                     WHEN skpi.unit = '%' THEN
                         COALESCE(
                             (SUM(d.numerator_kpi_value) / NULLIF(SUM(d.denominator_kpi_value), 0)) * 100,
                             CASE
                                 WHEN skpi.aggregation = 'SUM' THEN SUM(d.kpi_value)
                                 ELSE AVG(d.kpi_value)
                             END
                         )
                     ELSE
                         COALESCE(
                             (SUM(d.numerator_kpi_value) / NULLIF(SUM(d.denominator_kpi_value), 0)),
                             CASE
                                 WHEN skpi.aggregation = 'SUM' THEN SUM(d.kpi_value)
                                 ELSE AVG(d.kpi_value)
                             END
                         )
                 END AS kpi_value
             FROM kpi_values d
             JOIN standard_kpi skpi
                  ON skpi.id = d.standard_kpi_id
             WHERE d.standard_kpi_id = :standardKpiId
               AND d.rat_id = :ratId
               AND d.granularity_id = :granularityId
               AND d."timestamp" >= :startTimestamp
               AND d."timestamp" <=  :timestamp
             GROUP BY
                 date_trunc('day', d."timestamp"),
                 skpi.label,
                 skpi.unit,
                 skpi.aggregation
             ORDER BY "timestamp"
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


    @Query(value = """
            WITH filtered_kpis AS (
                SELECT k.*
                FROM kpi_values k
                JOIN district_codes dc ON k.district_code_id = dc.id
                JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
                WHERE k.standard_kpi_id = :standardKpiId
                    AND k.rat_id = :ratId
                    AND k.granularity_id = :granularityId
                    AND adcm.area_id = :areaId
                    AND k."timestamp" >= :startTimestamp
                    AND k."timestamp" <= :timestamp
            )
            SELECT
                date_trunc('day', k."timestamp") AS "timestamp",
                skpi.label AS kpi_label,
                CASE
                    WHEN skpi.unit = '%' THEN
                        COALESCE(
                            SUM(k.numerator_kpi_value) / NULLIF(SUM(k.denominator_kpi_value), 0) * 100,
                            CASE skpi.aggregation
                                WHEN 'SUM' THEN SUM(k.kpi_value)
                                ELSE AVG(k.kpi_value)
                            END
                        )
                    ELSE
                        COALESCE(
                            SUM(k.numerator_kpi_value) / NULLIF(SUM(k.denominator_kpi_value), 0),
                            CASE skpi.aggregation
                                WHEN 'SUM' THEN SUM(k.kpi_value)
                                ELSE AVG(k.kpi_value)
                            END
                        )
                END AS kpi_value
            FROM filtered_kpis k
            JOIN standard_kpi skpi
                ON skpi.id = k.standard_kpi_id
            GROUP BY
                date_trunc('day', k."timestamp"),
                skpi.label,
                skpi.unit,
                skpi.aggregation
            ORDER BY "timestamp";
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataByKpiAndArea(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("areaId") Long areaId, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


    @Query(value = """
            WITH filtered_kpis AS (
                SELECT k.*
                FROM kpi_values k
                JOIN district_codes dc ON k.district_code_id = dc.id
                JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
                JOIN cells c ON k.cell_name = c.cell_name
                JOIN bands b ON b.id = c.band_id
                WHERE k.standard_kpi_id = :standardKpiId
                    AND k.rat_id = :ratId
                    AND k.granularity_id = :granularityId
                    AND adcm.area_id = :areaId
                    AND k."timestamp" >= :startTimestamp
                    AND k."timestamp" <= :timestamp
                    AND b.id = :bandId
            )
            SELECT
                date_trunc('day', k."timestamp") AS "timestamp",
                skpi.label AS kpi_label,
                CASE
                    WHEN skpi.unit = '%' THEN
                        COALESCE(
                            SUM(k.numerator_kpi_value) / NULLIF(SUM(k.denominator_kpi_value), 0) * 100,
                            CASE skpi.aggregation
                                WHEN 'SUM' THEN SUM(k.kpi_value)
                                ELSE AVG(k.kpi_value)
                            END
                        )
                    ELSE
                        COALESCE(
                            SUM(k.numerator_kpi_value) / NULLIF(SUM(k.denominator_kpi_value), 0),
                            CASE skpi.aggregation
                                WHEN 'SUM' THEN SUM(k.kpi_value)
                                ELSE AVG(k.kpi_value)
                            END
                        )
                END AS kpi_value
            FROM filtered_kpis k
            JOIN standard_kpi skpi
                ON skpi.id = k.standard_kpi_id
            GROUP BY
                date_trunc('day', k."timestamp"),
                skpi.label,
                skpi.unit,
                skpi.aggregation
            ORDER BY "timestamp";
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataByKpiAreaAndBand(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("areaId") Long areaId, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId, @Param("bandId") Long bandId);


    @Query(value = """
            SELECT * FROM kpi_values
            WHERE district_code_id IS NULL AND rat_id = :ratId
            """, nativeQuery = true)
    List<KpiDay> findKpiWithoutDistrict(@Param("ratId") Long ratId);


    //============= Cell Name  =======================

    @Query(value = """
            SELECT DISTINCT ON (l.cell_name)
                   l.cell_name,
                   r.name  AS rat_name,
                   r.label AS rat_label
            FROM kpi_values l
            LEFT JOIN rat r ON r.id = l.rat_id
            ORDER BY l.cell_name ASC
            """, nativeQuery = true)
    List<CellNameDto> getAllCellNames();

    @Query(value = """
            SELECT l.cell_name,
                   r.name  AS rat_name,
                   r.label AS rat_label
            FROM kpi_values l
            JOIN rat r ON r.id = l.rat_id
            WHERE l.timestamp BETWEEN :preTimestamp AND :timestamp
                AND l.rat_id = :ratId
            	AND l.granularity_id = :granularityId
            GROUP BY l.cell_name, r.name, r.label
            """, nativeQuery = true)
    List<CellNameDto> getCellNamesByTimestamps(@Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);


    @Query(value = """
            SELECT l.cell_name,
            		null AS node_name,
            	   r.name  AS rat_name,
            	   s.site_code As site_code,
            	   b.name AS band_name,
            	   c.azimuth AS azimuth,
            	   c.beamwidth AS beamwidth,
            	   COALESCE(c.is_multi_beam, false) AS is_multi_beam,
            	   car.name AS carrier_name,
            	   sec.name AS sector_name
            FROM kpi_values l
            JOIN rat r ON r.id = l.rat_id
            LEFT JOIN cells c ON l.cell_name = c.cell_name
            LEFT JOIN bands b ON c.band_id = b.id
            LEFT JOIN sites s ON c.site_id = s.id
            LEFT JOIN carriers car ON car.id = c.carrier_id
            LEFT JOIN sectors sec ON sec.id = c.sector_id
            WHERE l.timestamp BETWEEN :preTimestamp AND :timestamp
                AND l.rat_id = :ratId
            	AND l.granularity_id = :granularityId
            GROUP BY l.cell_name, r.name, r.label,s.site_code, b.name,c.azimuth, c.beamwidth, c.is_multi_beam, car.name,sec.name;
            """, nativeQuery = true)
    List<CellDto> getCellsByTimestamp(
            @Param("timestamp") LocalDateTime timestamp,
            @Param("preTimestamp") LocalDateTime preTimestamp,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId
    );


    //===================== KPI REPORTS ================================================================================

    @Query(value = """
            WITH band_concat AS (
                SELECT
                    s.site_code,
                    STRING_AGG(d.band_name, '+' ORDER BY d.band_name ::int) AS concat_bands
                FROM (
                    SELECT DISTINCT
                        c.site_id,
                        b.name AS band_name
                    FROM cells c
                    JOIN bands b ON c.band_id = b.id
                    WHERE c.rat_id = :ratId
                ) d
                JOIN sites s ON s.id = d.site_id
                GROUP BY s.site_code
            )
            SELECT
                date_trunc('day', kv."timestamp") AS "timestamp",
                s.site_code,
            	skpi.kpi_name,
            	skpi.label,
                CASE skpi.aggregation
                    WHEN 'SUM' THEN SUM(kv.kpi_value)
                    ELSE AVG(kv.kpi_value)
                END AS kpi_value,
                bc.concat_bands
            FROM kpi_values kv
            JOIN standard_kpi skpi ON kv.standard_kpi_id = skpi.id
            JOIN cells c ON c.cell_name = kv.cell_name
            JOIN sites s ON s.id = c.site_id
            JOIN band_concat bc ON bc.site_code = s.site_code
            JOIN district_codes dc ON kv.district_code_id = dc.id
            JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
            WHERE kv.rat_id = :ratId
              AND kv.granularity_id = :granularityId
              AND kv.standard_kpi_id = :standardKpiId
              AND kv.timestamp BETWEEN :startTimestamp AND :endTimestamp
              AND adcm.area_id = :areaId
            GROUP BY
                date_trunc('day', kv."timestamp"),
                s.site_code,
                bc.concat_bands,
                skpi.aggregation,
            	skpi.label,
            	skpi.kpi_name;
            """, nativeQuery = true)
    List<SiteKpiReportDto> getSiteWiseReportByKpiAndDate(@Param("ratId") Long ratId, @Param("granularityId") Long granularityId, @Param("standardKpiId") Long standardKpiId, @Param("startTimestamp") LocalDateTime startTimestamp, @Param("endTimestamp") LocalDateTime endTimestamp, @Param("areaId") Long areaId);


    @Modifying
    @Transactional
    @Query(value = """
            WITH scored AS (
                SELECT
                    id, timestamp, cell_name, standard_kpi_id, rat_id, granularity_id, oss_id,
                    CASE
                        WHEN numerator_kpi_value IS NOT NULL
                             AND denominator_kpi_value IS NOT NULL
                             AND NOT (numerator_kpi_value = 0 AND denominator_kpi_value = 0)
                        THEN 0
                        WHEN kpi_value IS NOT NULL
                             AND NOT (
                                 COALESCE(numerator_kpi_value, 0) = 0
                                 AND COALESCE(denominator_kpi_value, 0) = 0
                                 AND kpi_value = 100
                             )
                        THEN 1
                        ELSE 2
                    END AS tier
                FROM kpi_values
                WHERE timestamp >= :startTimestamp AND timestamp < :endTimestamp
            ),
            ranked AS (
                SELECT s.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY cell_name, standard_kpi_id, rat_id, granularity_id, timestamp
                           ORDER BY tier, id
                       ) AS rn
                FROM scored s
            )
            DELETE FROM kpi_values kv
            USING ranked r
            WHERE kv.id = r.id
              AND kv.timestamp = r.timestamp
              AND kv.timestamp >= :startTimestamp AND kv.timestamp < :endTimestamp
              AND r.rn > 1;
            """, nativeQuery = true)
    int deduplicateOssByPeriod(@Param("startTimestamp") LocalDateTime startTimestamp, @Param("endTimestamp") LocalDateTime endTimestamp);
}