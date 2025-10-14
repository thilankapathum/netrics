package dev.thilanka.netrics.repository;

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
            SELECT * FROM
                (
                    SELECT
                        curr.cell_name, curr.kpi_label, curr.unit, curr.value,
                        pre.previous_value, (curr.value - pre.previous_value) AS difference,
                        CASE
                            WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
                            WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
                            ELSE 0
                        END AS improved
                    FROM (
                        SELECT
                            cell_name,
                            lte_fdd_standard_kpi.label AS kpi_label,
                            lte_fdd_standard_kpi.unit AS unit,
                            lte_fdd_standard_kpi.worst_order AS worst_order,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0)) * 100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                END,
                                AVG(kpi_value)
                            ) AS value
                        FROM lte_fdd_kpi_day
                        LEFT JOIN lte_fdd_standard_kpi
                            ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                          AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                          AND lte_fdd_kpi_day.rat_id = :ratId
                        GROUP BY cell_name, lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
                    ) AS curr
                    LEFT JOIN (
                        SELECT
                            cell_name AS pre_cell_name,
                            lte_fdd_standard_kpi.unit AS unit,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0)) * 100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                END,
                                AVG(kpi_value)
                            ) AS previous_value
                        FROM lte_fdd_kpi_day
                        LEFT JOIN lte_fdd_standard_kpi
                            ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                          AND "timestamp" BETWEEN (:preTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :preTimestamp
                          AND lte_fdd_kpi_day.rat_id = :ratId
                        GROUP BY cell_name, lte_fdd_standard_kpi.unit
                    ) AS pre
                    ON curr.cell_name = pre.pre_cell_name
                    ORDER BY
                        CASE WHEN curr.worst_order = 'ASC' THEN curr.value END ASC,
                        CASE WHEN curr.worst_order = 'DESC' THEN curr.value END DESC
                    LIMIT 25
                ) AS top100
            """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                            SELECT curr.cell_name
                            FROM (
                                SELECT
                                    cell_name,
                                    COALESCE(
                                        CASE
                                            WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0)) * 100
                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                        END,
                                        AVG(kpi_value)
                                    ) AS value,
                                    lte_fdd_standard_kpi.worst_order
                                FROM lte_fdd_kpi_day
                                LEFT JOIN lte_fdd_standard_kpi
                                    ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                                  AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                                  AND lte_fdd_kpi_day.rat_id = :ratId
                                GROUP BY cell_name, lte_fdd_standard_kpi.worst_order, lte_fdd_standard_kpi.unit
                                ORDER BY
                                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'ASC'
                                                 THEN COALESCE(
                                                        CASE
                                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                                        END,
                                                        AVG(kpi_value)
                                                      )
                                            END ASC,
                                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'DESC'
                                                 THEN COALESCE(
                                                        CASE
                                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                                        END,
                                                        AVG(kpi_value)
                                                      )
                                            END DESC
                                LIMIT 25
                            ) AS curr
                        ) AS count_query
            """, nativeQuery = true)
    List<WorstCellsDto> findWorstCells(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("ratId") Long ratId);

    @Query(value = """
            SELECT * FROM
                (
                    SELECT
                        curr.cell_name, curr.kpi_label, curr.unit, curr.value,
                        pre.previous_value, (curr.value - pre.previous_value) AS difference,
                        CASE
                            WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
                            WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
                            ELSE 0
                        END AS improved
                    FROM (
                        SELECT
                            cell_name,
                            lte_fdd_standard_kpi.label AS kpi_label,
                            lte_fdd_standard_kpi.unit AS unit,
                            lte_fdd_standard_kpi.worst_order AS worst_order,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
                        GROUP BY cell_name, lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
                    ) AS curr
                    LEFT JOIN (
                        SELECT
                            cell_name AS pre_cell_name,
                            lte_fdd_standard_kpi.unit AS unit,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
                        GROUP BY cell_name, lte_fdd_standard_kpi.unit
                    ) AS pre
                    ON curr.cell_name = pre.pre_cell_name
                    ORDER BY
                        CASE WHEN curr.worst_order = 'ASC' THEN curr.value END ASC,
                        CASE WHEN curr.worst_order = 'DESC' THEN curr.value END DESC
                    LIMIT 25
                ) AS top100
            """, countQuery = """
            SELECT COUNT(*) FROM (
                    SELECT curr.cell_name
                    FROM (
                        SELECT
                            cell_name,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
                          AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                          AND d.id = :districtId
                          AND lte_fdd_kpi_day.rat_id = :ratId
                        GROUP BY cell_name, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
                        ORDER BY
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'ASC'
                                 THEN COALESCE(
                                        CASE
                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                        END,
                                        AVG(kpi_value)
                                      )
                            END ASC,
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'DESC'
                                 THEN COALESCE(
                                        CASE
                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                        END,
                                        AVG(kpi_value)
                                      )
                            END DESC
                        LIMIT 25
                    ) AS curr
                ) AS count_query
            """, nativeQuery = true)
    List<WorstCellsDto> findWorstCellsByDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);

// --- EXCLUDING ZEROES

    @Query(value = """
            SELECT * FROM
                (
                    SELECT
                        curr.cell_name, curr.kpi_label, curr.unit, curr.value,
                        pre.previous_value, (curr.value - pre.previous_value) AS difference,
                        CASE
                            WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
                            WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
                            ELSE 0
                        END AS improved
                    FROM (
                        SELECT
                            cell_name,
                            lte_fdd_standard_kpi.label AS kpi_label,
                            lte_fdd_standard_kpi.unit AS unit,
                            lte_fdd_standard_kpi.worst_order AS worst_order,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0)) * 100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                END,
                                AVG(kpi_value)
                            ) AS value
                        FROM lte_fdd_kpi_day
                        LEFT JOIN lte_fdd_standard_kpi
                            ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                          AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                          AND lte_fdd_kpi_day.rat_id = :ratId
                        GROUP BY cell_name, lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
                    ) AS curr
                    LEFT JOIN (
                        SELECT
                            cell_name AS pre_cell_name,
                            lte_fdd_standard_kpi.unit AS unit,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0)) * 100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                END,
                                AVG(kpi_value)
                            ) AS previous_value
                        FROM lte_fdd_kpi_day
                        LEFT JOIN lte_fdd_standard_kpi
                            ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                        WHERE lte_fdd_standard_kpi_id = :standardKpiId
                          AND "timestamp" BETWEEN (:preTimestamp ::DATE - (:period * INTERVAL '1 day')) AND :preTimestamp
                          AND lte_fdd_kpi_day.rat_id = :ratId
                        GROUP BY cell_name, lte_fdd_standard_kpi.unit
                    ) AS pre
                    ON curr.cell_name = pre.pre_cell_name
                    WHERE value != 0
                    ORDER BY
                        CASE WHEN curr.worst_order = 'ASC' THEN curr.value END ASC,
                        CASE WHEN curr.worst_order = 'DESC' THEN curr.value END DESC
                    LIMIT 25
                ) AS top100
            """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                            SELECT curr.cell_name
                            FROM (
                                SELECT
                                    cell_name,
                                    COALESCE(
                                        CASE
                                            WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0)) * 100
                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                        END,
                                        AVG(kpi_value)
                                    ) AS value,
                                    lte_fdd_standard_kpi.worst_order
                                FROM lte_fdd_kpi_day
                                LEFT JOIN lte_fdd_standard_kpi
                                    ON lte_fdd_kpi_day.lte_fdd_standard_kpi_id = lte_fdd_standard_kpi.id
                                WHERE lte_fdd_standard_kpi_id = :standardKpiId
                                  AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                                  AND lte_fdd_kpi_day.rat_id = ratId
                                GROUP BY cell_name, lte_fdd_standard_kpi.worst_order, lte_fdd_standard_kpi.unit
                                ORDER BY
                                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'ASC'
                                                 THEN COALESCE(
                                                        CASE
                                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                                        END,
                                                        AVG(kpi_value)
                                                      )
                                            END ASC,
                                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'DESC'
                                                 THEN COALESCE(
                                                        CASE
                                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                                        END,
                                                        AVG(kpi_value)
                                                      )
                                            END DESC
                                LIMIT 25
                            ) AS curr
                        ) AS count_query
            """, nativeQuery = true)
    List<WorstCellsDto> findWorstCellsExcludeZeroes(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("ratId") Long ratId);

    @Query(value = """
            SELECT * FROM
                (
                    SELECT
                        curr.cell_name, curr.kpi_label, curr.unit, curr.value,
                        pre.previous_value, (curr.value - pre.previous_value) AS difference,
                        CASE
                            WHEN curr.worst_order = 'ASC' AND (curr.value - pre.previous_value) > 0 THEN 1
                            WHEN curr.worst_order = 'DESC' AND (curr.value - pre.previous_value) < 0 THEN 1
                            ELSE 0
                        END AS improved
                    FROM (
                        SELECT
                            cell_name,
                            lte_fdd_standard_kpi.label AS kpi_label,
                            lte_fdd_standard_kpi.unit AS unit,
                            lte_fdd_standard_kpi.worst_order AS worst_order,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
                        GROUP BY cell_name, lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
                    ) AS curr
                    LEFT JOIN (
                        SELECT
                            cell_name AS pre_cell_name,
                            lte_fdd_standard_kpi.unit AS unit,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
                        GROUP BY cell_name, lte_fdd_standard_kpi.unit
                    ) AS pre
                    ON curr.cell_name = pre.pre_cell_name
                    WHERE value != 0
                    ORDER BY
                        CASE WHEN curr.worst_order = 'ASC' THEN curr.value END ASC,
                        CASE WHEN curr.worst_order = 'DESC' THEN curr.value END DESC
                    LIMIT 25
                ) AS top100
            """, countQuery = """
            SELECT COUNT(*) FROM (
                    SELECT curr.cell_name
                    FROM (
                        SELECT
                            cell_name,
                            COALESCE(
                                CASE
                                    WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                    ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
                          AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
                          AND d.id = :districtId
                          AND lte_fdd_kpi_day.rat_id = :ratId
                        GROUP BY cell_name, lte_fdd_standard_kpi.unit, lte_fdd_standard_kpi.worst_order
                        ORDER BY
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'ASC'
                                 THEN COALESCE(
                                        CASE
                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                        END,
                                        AVG(kpi_value)
                                      )
                            END ASC,
                            CASE WHEN lte_fdd_standard_kpi.worst_order = 'DESC'
                                 THEN COALESCE(
                                        CASE
                                            WHEN lte_fdd_standard_kpi.unit = '%'
                                            THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                                            ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                                        END,
                                        AVG(kpi_value)
                                      )
                            END DESC
                        LIMIT 25
                    ) AS curr
                ) AS count_query
            """, nativeQuery = true)
    List<WorstCellsDto> findWorstCellsByDistrictExcludeZeroes(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("preTimestamp") LocalDateTime preTimestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);


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
            SELECT
                "timestamp",
                lte_fdd_standard_kpi.label AS kpi_label,
                COALESCE(
                    CASE
                        WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                        ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                    END,
                    AVG(kpi_value)
                ) AS kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
                ON lte_fdd_standard_kpi.id = lte_fdd_standard_kpi_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
              AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
              AND lte_fdd_kpi_day.rat_id = :ratId
            GROUP BY "timestamp", lte_fdd_standard_kpi_id, lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataAvgByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("ratId") Long ratId);

    @Query(value = """
            SELECT\s
                "timestamp",
                lte_fdd_standard_kpi.label AS kpi_label,
                COALESCE(
                    CASE
                        WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                        ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
                    END,
                    SUM(kpi_value)
                ) AS kpi_value
            FROM lte_fdd_kpi_day
            LEFT JOIN lte_fdd_standard_kpi
                ON lte_fdd_standard_kpi.id = lte_fdd_standard_kpi_id
            WHERE lte_fdd_standard_kpi_id = :standardKpiId
              AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
              AND lte_fdd_kpi_day.rat_id = :ratId
            GROUP BY "timestamp", lte_fdd_standard_kpi_id, lte_fdd_standard_kpi.label, lte_fdd_standard_kpi.unit
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataSumByKpi(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("ratId") Long ratId);


    @Query(value = """
            SELECT
                "timestamp",
                lte_fdd_standard_kpi.label AS kpi_label,
                COALESCE(
                    CASE
                        WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                        ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
              AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
              AND d.id = :districtId
              AND lte_fdd_kpi_day.rat_id = :ratId
            GROUP BY "timestamp", lte_fdd_standard_kpi_id, lte_fdd_standard_kpi.label,lte_fdd_standard_kpi.unit;
            
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataAvgByKpiAndDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);

    @Query(value = """
            SELECT\s
                "timestamp",
                lte_fdd_standard_kpi.label AS kpi_label,
                COALESCE(
                    CASE
                        WHEN lte_fdd_standard_kpi.unit = '%' THEN (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))*100
                        ELSE (SUM(numerator_kpi_value) / NULLIF(SUM(denominator_kpi_value),0))
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
              AND "timestamp" BETWEEN (:timestamp ::DATE - (:period * INTERVAL '1 day')) AND :timestamp
              AND d.id = :districtId
              AND lte_fdd_kpi_day.rat_id = :ratId
            GROUP BY "timestamp", lte_fdd_standard_kpi_id, lte_fdd_standard_kpi.label,lte_fdd_standard_kpi.unit;
            
            """, nativeQuery = true)
    List<KpiTrend> findTrendDataSumByKpiAndDistrict(@Param("standardKpiId") Long standardKpiId, @Param("timestamp") LocalDateTime timestamp, @Param("period") Long period, @Param("districtId") Long districtId, @Param("ratId") Long ratId);






    @Query(value = """
            SELECT * FROM lte_fdd_kpi_day
            WHERE district_code_id IS NULL AND rat_id = :ratId
            """, nativeQuery = true)
    List<KpiDay> findKpiWithoutDistrict(@Param("ratId") Long ratId);
}
