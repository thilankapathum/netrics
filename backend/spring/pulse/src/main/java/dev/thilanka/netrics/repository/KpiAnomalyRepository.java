package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.BreachingCellProjection;
import dev.thilanka.netrics.dto.CellSeverityProjection;
import dev.thilanka.netrics.dto.KpiAnomalyProjection;
import dev.thilanka.netrics.entity.KpiAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiAnomalyRepository extends JpaRepository<KpiAnomaly, Long> {

    @Modifying
    @Query(value = "SET LOCAL work_mem = :workMem", nativeQuery = true)
    void setLocalWorkMem(@Param("workMem") String workMem);

    @Modifying
    @Query(value = "SET LOCAL work_mem = '64MB'", nativeQuery = true)
    void setWorkMem64();

    @Query(value = """
        SELECT kv.cell_name AS cellName, kv.standard_kpi_id AS standardKpiId
        FROM kpi_values kv
        JOIN standard_kpi sk ON sk.id = kv.standard_kpi_id
        WHERE kv.timestamp BETWEEN :currStart AND :currEnd
          AND kv.rat_id = :ratId
          AND kv.granularity_id = :granularityId
          AND sk.threshold IS NOT NULL
          AND (
            (sk.worst_order = 'ASC' AND COALESCE(
                CASE WHEN sk.unit = '%' THEN (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) * 100
                     ELSE (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) END,
                kv.kpi_value) < sk.threshold)
            OR
            (sk.worst_order = 'DESC' AND COALESCE(
                CASE WHEN sk.unit = '%' THEN (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) * 100
                     ELSE (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) END,
                kv.kpi_value) > sk.threshold)
          )
        """, nativeQuery = true)
    List<BreachingCellProjection> findBreachingCells(@Param("currStart") LocalDateTime currStart,
                                                     @Param("currEnd") LocalDateTime currEnd,
                                                     @Param("ratId") Long ratId,
                                                     @Param("granularityId") Long granularityId);

    @Modifying
    @Query(value = """
            WITH pairs AS (
                SELECT * FROM unnest(:cellNames ::varchar[], :kpiIds ::bigint[])
                    AS p(cell_name, standard_kpi_id)
            ),
            current_point AS MATERIALIZED (
                SELECT
                    pairs.cell_name, pairs.standard_kpi_id, sk.worst_order,
                    kv.timestamp AS observed_timestamp,
                    COALESCE(
                        CASE WHEN sk.unit = '%' THEN (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) * 100
                             ELSE (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) END,
                        kv.kpi_value) AS observed_value
                FROM pairs
                JOIN kpi_values kv
                    ON kv.cell_name = pairs.cell_name AND kv.standard_kpi_id = pairs.standard_kpi_id
                   AND kv.rat_id = :ratId AND kv.granularity_id = :granularityId
                   AND kv.timestamp BETWEEN :currStart ::timestamp AND :currEnd ::timestamp
                JOIN standard_kpi sk ON sk.id = pairs.standard_kpi_id
            ),
            history AS MATERIALIZED (
                -- Carry observed_value/observed_timestamp through — constant per (cell_name, standard_kpi_id) group
                SELECT
                    cp.cell_name, cp.standard_kpi_id, cp.observed_value, cp.observed_timestamp,
                    COALESCE(
                        CASE WHEN sk.unit = '%' THEN (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) * 100
                             ELSE (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) END,
                        kv.kpi_value) AS hist_value
                FROM current_point cp
                JOIN kpi_values kv
                    ON kv.cell_name = cp.cell_name AND kv.standard_kpi_id = cp.standard_kpi_id
                   AND kv.rat_id = :ratId AND kv.granularity_id = :granularityId
                   AND kv.timestamp < :currStart ::timestamp
                   AND kv.timestamp >= :currStart ::timestamp - (:baselineDays * INTERVAL '1 day')
                JOIN standard_kpi sk ON sk.id = cp.standard_kpi_id
            ),
            median_calc AS MATERIALIZED (
                SELECT cell_name, standard_kpi_id,
                       MAX(observed_value) AS observed_value,        -- constant per group, MAX just extracts it
                       MAX(observed_timestamp) AS observed_timestamp,
                       COUNT(*) AS n,
                       array_agg(hist_value ORDER BY hist_value) AS sorted_values
                FROM history
                GROUP BY cell_name, standard_kpi_id
            ),
            median_final AS MATERIALIZED (
                SELECT cell_name, standard_kpi_id, observed_value, observed_timestamp, n, sorted_values,
                    CASE WHEN n % 2 = 1 THEN sorted_values[(n + 1) / 2]
                         ELSE (sorted_values[n / 2] + sorted_values[n / 2 + 1]) / 2.0
                    END AS median_value
                FROM median_calc
            ),
            deviations AS MATERIALIZED (
                SELECT mf.cell_name, mf.standard_kpi_id, mf.observed_value, mf.observed_timestamp,
                       mf.n, mf.median_value,
                       ABS(hv.hist_value - mf.median_value) AS abs_dev
                FROM median_final mf
                CROSS JOIN LATERAL unnest(mf.sorted_values) AS hv(hist_value)
            ),
            mad_calc AS MATERIALIZED (
                SELECT cell_name, standard_kpi_id, observed_value, observed_timestamp, n, median_value,
                       array_agg(abs_dev ORDER BY abs_dev) AS sorted_devs,
                       AVG(abs_dev) AS mean_ad
                FROM deviations
                GROUP BY cell_name, standard_kpi_id, observed_value, observed_timestamp, n, median_value
            ),
            mad_final AS MATERIALIZED (
                SELECT cell_name, standard_kpi_id, observed_value, observed_timestamp, n, median_value, mean_ad,
                    CASE WHEN n % 2 = 1 THEN sorted_devs[(n + 1) / 2]
                         ELSE (sorted_devs[n / 2] + sorted_devs[n / 2 + 1]) / 2.0
                    END AS mad
                FROM mad_calc
            ),
            -- No join needed at all now — everything scored needs is already in mad_final
            scored AS MATERIALIZED (
                SELECT
                    cell_name, standard_kpi_id, observed_timestamp, observed_value, median_value, n,
                    CASE WHEN mad > 0 THEN mad WHEN mean_ad > 0 THEN mean_ad ELSE NULL END AS effective_mad,
                    CASE
                        WHEN mad > 0     THEN 0.6745 * (observed_value - median_value) / mad
                        WHEN mean_ad > 0 THEN 0.7979 * (observed_value - median_value) / mean_ad
                        ELSE NULL
                    END AS robust_z_score,
                    (mad = 0 AND mean_ad = 0 AND observed_value <> median_value) AS is_constant_break
                FROM mad_final
                WHERE n >= :minHistory
            )
            INSERT INTO kpi_anomalies
                (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp,
                 observed_value, baseline_median, mad, robust_z_score, severity, detected_at)
            SELECT
                cell_name, standard_kpi_id, :ratId, :granularityId, observed_timestamp,
                observed_value, median_value, effective_mad, robust_z_score,
                CASE
                    WHEN is_constant_break THEN 'high'
                    WHEN ABS(robust_z_score) >= 6 THEN 'critical'
                    WHEN ABS(robust_z_score) >= 4 THEN 'high'
                    ELSE 'moderate'
                END,
                NOW()
            FROM scored
            WHERE is_constant_break
               OR (effective_mad IS NOT NULL AND ABS(robust_z_score) >= :zThreshold)
            ON CONFLICT (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp) DO NOTHING
            """, nativeQuery = true)
    int detectAndInsertAnomalies(@Param("cellNames") String[] cellNames,
                                 @Param("kpiIds") Long[] kpiIds,
                                 @Param("ratId") Long ratId,
                                 @Param("granularityId") Long granularityId,
                                 @Param("currStart") LocalDateTime currStart,
                                 @Param("currEnd") LocalDateTime currEnd,
                                 @Param("baselineDays") int baselineDays,
                                 @Param("minHistory") int minHistory,
                                 @Param("zThreshold") double zThreshold);


//    @Query(value = """
//        SELECT MAX(timestamp) FROM kpi_anomalies
//        WHERE rat_id = :ratId AND granularity_id = :granularityId
//        """, nativeQuery = true)
//    LocalDateTime getLatestAnomalyTimestamp(@Param("ratId") Long ratId, @Param("granularityId") Long granularityId);

    @Query(value = """
        SELECT ka.cell_name AS cellName,
               sk.kpi_name AS kpiName,
               sk.label AS kpiLabel,
               sk.unit AS unit,
               ka.timestamp AS timestamp,
               ka.observed_value AS observedValue,
               ka.baseline_median AS baselineMedian,
               ka.mad AS mad,
               ka.robust_z_score AS robustZScore,
               ka.severity AS severity
        FROM kpi_anomalies ka
        JOIN standard_kpi sk ON sk.id = ka.standard_kpi_id
        WHERE ka.rat_id = :ratId
          AND ka.granularity_id = :granularityId
          AND ka.timestamp = :timestamp
        ORDER BY
            CASE ka.severity WHEN 'critical' THEN 3 WHEN 'high' THEN 2 ELSE 1 END DESC,
            ABS(ka.robust_z_score) DESC
        """, nativeQuery = true)
    List<KpiAnomalyProjection> findAnomaliesByTimestamp(@Param("timestamp") LocalDateTime timestamp,
                                                        @Param("ratId") Long ratId,
                                                        @Param("granularityId") Long granularityId);


    @Query(value = """
            SELECT cell_name AS cellName, severity AS severity
            FROM kpi_anomalies
            WHERE standard_kpi_id = :standardKpiId
                AND rat_id = :ratId
                AND granularity_id = :granularityId
                AND timestamp BETWEEN :currStart AND :currEnd
                AND cell_name = ANY(:cellNames ::varchar[])
        """, nativeQuery = true)
    List<CellSeverityProjection> findSeveritiesForCells(
            @Param("standardKpiId") Long standardKpiId,
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("cellNames") String[] cellNames,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId);


    @Query(value = """
            SELECT ka.cell_name AS cellName,
                   sk.kpi_name AS kpiName,
                   sk.label AS kpiLabel,
                   sk.unit AS unit,
                   ka.timestamp AS timestamp,
                   ka.observed_value AS observedValue,
                   ka.baseline_median AS baselineMedian,
                   ka.mad AS mad,
                   ka.robust_z_score AS robustZScore,
                   ka.severity AS severity
            FROM kpi_anomalies ka
            JOIN standard_kpi sk ON sk.id = ka.standard_kpi_id
            WHERE ka.rat_id = :ratId
              AND ka.granularity_id = :granularityId
              AND ka.timestamp BETWEEN :currStart AND :currEnd
            ORDER BY
                CASE ka.severity WHEN 'critical' THEN 3 WHEN 'high' THEN 2 ELSE 1 END DESC,
                ABS(ka.robust_z_score) DESC
            """, nativeQuery = true)
    List<KpiAnomalyProjection> findAnomaliesByDateRange(@Param("currStart") LocalDateTime currStart,
                                                        @Param("currEnd") LocalDateTime currEnd,
                                                        @Param("ratId") Long ratId,
                                                        @Param("granularityId") Long granularityId);
}
