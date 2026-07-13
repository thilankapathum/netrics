package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.KpiAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiAnomalyRepository extends JpaRepository<KpiAnomaly, Long> {

    @Query(value = "SELECT DISTINCT timestamp FROM kpi_anomalies WHERE kpi_anomalies.rat_id = :ratId AND kpi_anomalies.granularity_id = :granularityId ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLatestDate(@Param("ratId") Long ratId, @Param("granularityId") Long granularityId);

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


    @Query(value = """
        WITH params AS (
            SELECT
                :prevPeriodDays ::int       AS prev_period_days,
                :areaId        ::bigint    AS area_id,
                :ratId         ::bigint    AS rat_id,
                :granularityId ::bigint    AS granularity_id,
                :currStart     ::timestamp AS curr_start,
                :currEnd       ::timestamp AS curr_end
        ),
        area_cells AS MATERIALIZED (
            SELECT c.cell_name
            FROM cells c
            JOIN district_codes dc ON dc.id = c.district_code_id
            JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
            WHERE adcm.area_id = (SELECT area_id FROM params)
        ),
        anomalies AS MATERIALIZED (
            SELECT ka.cell_name, ka.standard_kpi_id, ka.observed_value, ka.severity
            FROM kpi_anomalies ka
            JOIN area_cells ac ON ac.cell_name = ka.cell_name
            CROSS JOIN params p
            WHERE ka.rat_id = p.rat_id
              AND ka.granularity_id = p.granularity_id
              AND ka.timestamp >= p.curr_start
              AND ka.timestamp <  p.curr_end
        ),
        kpi_meta AS MATERIALIZED (
            SELECT id, kpi_name, label, unit, worst_order
            FROM standard_kpi
            WHERE id IN (SELECT DISTINCT standard_kpi_id FROM anomalies)
        ),
        prev_agg AS MATERIALIZED (
            SELECT kv.cell_name, kv.standard_kpi_id,
                   SUM(kv.numerator_kpi_value) AS prev_num,
                   SUM(kv.denominator_kpi_value) AS prev_den,
                   AVG(kv.kpi_value) AS prev_avg
            FROM kpi_values kv
            JOIN anomalies a ON a.cell_name = kv.cell_name AND a.standard_kpi_id = kv.standard_kpi_id
            CROSS JOIN params p
            WHERE kv.rat_id = p.rat_id
              AND kv.granularity_id = p.granularity_id
              AND kv.timestamp >= p.curr_start - (p.prev_period_days * INTERVAL '1 day')
              AND kv.timestamp <  p.curr_start
            GROUP BY kv.cell_name, kv.standard_kpi_id
        ),
        calc AS MATERIALIZED (
            SELECT
                a.cell_name, a.observed_value, a.severity,
                km.kpi_name, km.label, km.unit, km.worst_order,
                CASE
                    WHEN km.unit = '%' THEN COALESCE((pa.prev_num / NULLIF(pa.prev_den, 0)) * 100, pa.prev_avg)
                    ELSE                    COALESCE((pa.prev_num / NULLIF(pa.prev_den, 0)),        pa.prev_avg)
                END AS previous_value
            FROM anomalies a
            JOIN kpi_meta km ON km.id = a.standard_kpi_id
            LEFT JOIN prev_agg pa ON pa.cell_name = a.cell_name AND pa.standard_kpi_id = a.standard_kpi_id
        )
        SELECT
            cell_name    AS cellName,
            kpi_name     AS kpiName,
            label        AS kpiLabel,
            unit         AS unit,
            observed_value AS value,
            previous_value AS previousValue,
            (observed_value - previous_value) AS difference,
            CASE
                WHEN worst_order = 'ASC'  AND observed_value > previous_value THEN 1
                WHEN worst_order = 'DESC' AND observed_value < previous_value THEN 1
                ELSE 0
            END AS improved,
            severity AS severity
        FROM calc
        """, nativeQuery = true)
    List<AnomalyCellsProjection> findAllAnomalyCellsByArea(
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("prevPeriodDays") int prevPeriodDays,
            @Param("areaId") Long areaId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId);


    //--------- PAGED ------------------

    @Query(value = """
    WITH params AS (
        SELECT
            :areaId        ::bigint    AS area_id,
            :ratId         ::bigint    AS rat_id,
            :granularityId ::bigint    AS granularity_id,
            :currStart     ::timestamp AS curr_start,
            :currEnd       ::timestamp AS curr_end,
            :prevStart     ::timestamp AS prev_start,
            :prevEnd       ::timestamp AS prev_end
    ),
    area_cells AS MATERIALIZED (
        SELECT c.cell_name
        FROM cells c
        JOIN district_codes dc ON dc.id = c.district_code_id
        JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
        WHERE adcm.area_id = (SELECT area_id FROM params)
    ),
    anomalies AS MATERIALIZED (
        SELECT ka.cell_name, ka.standard_kpi_id, ka.observed_value, ka.severity
        FROM kpi_anomalies ka
        JOIN area_cells ac ON ac.cell_name = ka.cell_name
        CROSS JOIN params p
        WHERE ka.rat_id = p.rat_id
          AND ka.granularity_id = p.granularity_id
          AND ka.timestamp >= p.curr_start
          AND ka.timestamp <=  p.curr_end
          AND (:severityFilter IS NULL OR ka.severity = :severityFilter)
          AND (:standardKpiId IS NULL OR ka.standard_kpi_id = :standardKpiId)
    ),
    kpi_meta AS MATERIALIZED (
        SELECT id, kpi_name, label, unit, worst_order
        FROM standard_kpi
        WHERE id IN (SELECT DISTINCT standard_kpi_id FROM anomalies)
    ),
    prev_agg AS MATERIALIZED (
        SELECT kv.cell_name, kv.standard_kpi_id,
               SUM(kv.numerator_kpi_value) AS prev_num,
               SUM(kv.denominator_kpi_value) AS prev_den,
               AVG(kv.kpi_value) AS prev_avg
        FROM kpi_values kv
        JOIN anomalies a ON a.cell_name = kv.cell_name AND a.standard_kpi_id = kv.standard_kpi_id
        CROSS JOIN params p
        WHERE kv.rat_id = p.rat_id
          AND kv.granularity_id = p.granularity_id
          AND kv.timestamp >= p.prev_start
          AND kv.timestamp <=  p.prev_end
        GROUP BY kv.cell_name, kv.standard_kpi_id
    ),
    calc AS MATERIALIZED (
        SELECT
            a.cell_name, a.observed_value, a.severity,
            km.kpi_name, km.label, km.unit, km.worst_order,
            CASE
                WHEN km.unit = '%' THEN COALESCE((pa.prev_num / NULLIF(pa.prev_den, 0)) * 100, pa.prev_avg)
                ELSE                    COALESCE((pa.prev_num / NULLIF(pa.prev_den, 0)),        pa.prev_avg)
            END AS previous_value
        FROM anomalies a
        JOIN kpi_meta km ON km.id = a.standard_kpi_id
        LEFT JOIN prev_agg pa ON pa.cell_name = a.cell_name AND pa.standard_kpi_id = a.standard_kpi_id
    )
    SELECT
        cell_name    AS cellName,
        kpi_name     AS kpiName,
        label        AS kpiLabel,
        unit         AS unit,
        observed_value AS value,
        previous_value AS previousValue,
        (observed_value - previous_value) AS difference,
        CASE
            WHEN worst_order = 'ASC'  AND observed_value > previous_value THEN 1
            WHEN worst_order = 'DESC' AND observed_value < previous_value THEN 1
            ELSE 0
        END AS improved,
        severity AS severity
    FROM calc
    ORDER BY
        CASE WHEN :sortBy = 'severity' AND :sortDir = 'desc'
             THEN CASE severity WHEN 'critical' THEN 3 WHEN 'high' THEN 2 ELSE 1 END END DESC,
        CASE WHEN :sortBy = 'severity' AND :sortDir = 'asc'
             THEN CASE severity WHEN 'critical' THEN 3 WHEN 'high' THEN 2 ELSE 1 END END ASC,
        CASE WHEN :sortBy = 'difference' AND :sortDir = 'desc' THEN ABS(observed_value - previous_value) END DESC,
        CASE WHEN :sortBy = 'difference' AND :sortDir = 'asc'  THEN ABS(observed_value - previous_value) END ASC,
        CASE WHEN :sortBy = 'value' AND :sortDir = 'desc' THEN observed_value END DESC,
        CASE WHEN :sortBy = 'value' AND :sortDir = 'asc'  THEN observed_value END ASC,
        CASE WHEN :sortBy = 'cellName' AND :sortDir = 'desc' THEN cell_name END DESC,
        CASE WHEN :sortBy = 'cellName' AND :sortDir = 'asc'  THEN cell_name END ASC
    LIMIT :pageSize OFFSET :offset
    """, nativeQuery = true)
    List<AnomalyCellsProjection> findAllAnomalyCellsByAreaPaged(
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("prevStart") LocalDateTime prevStart,
            @Param("prevEnd") LocalDateTime prevEnd,
            @Param("areaId") Long areaId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId,
            @Param("severityFilter") String severityFilter,
            @Param("standardKpiId") Long standardKpiId,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset);

    @Query(value = """
    SELECT COUNT(*)
    FROM kpi_anomalies ka
    JOIN cells c ON c.cell_name = ka.cell_name
    JOIN district_codes dc ON dc.id = c.district_code_id
    JOIN area_district_code_mapping adcm ON dc.id = adcm.district_code_id
    WHERE adcm.area_id = :areaId
      AND ka.rat_id = :ratId
      AND ka.granularity_id = :granularityId
      AND ka.timestamp >= :currStart
      AND ka.timestamp <=  :currEnd
      AND (:severityFilter IS NULL OR ka.severity = :severityFilter)
      AND (:standardKpiId IS NULL OR ka.standard_kpi_id = :standardKpiId)
    """, nativeQuery = true)
    long countAnomalyCellsByArea(
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("areaId") Long areaId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId,
            @Param("severityFilter") String severityFilter,
            @Param("standardKpiId") Long standardKpiId);


    @Query(value = """
    SELECT sk.kpi_name AS kpiName, sk.label AS kpiLabel, ka.severity AS severity, COUNT(*) AS cnt
    FROM kpi_anomalies ka
    JOIN standard_kpi sk ON sk.id = ka.standard_kpi_id
    JOIN cells c ON c.cell_name = ka.cell_name
    JOIN district_codes dc ON dc.id = c.district_code_id
    JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
    WHERE adcm.area_id = :areaId
      AND ka.rat_id = :ratId
      AND ka.granularity_id = :granularityId
      AND ka.timestamp >= :currStart
      AND ka.timestamp <=  :currEnd
      AND (:standardKpiId IS NULL OR ka.standard_kpi_id = :standardKpiId)
    GROUP BY sk.kpi_name, sk.label, ka.severity
    """, nativeQuery = true)
    List<AnomalySeverityCountProjection> countAnomaliesByKpiAndSeverity(
            @Param("currStart") LocalDateTime currStart,
            @Param("currEnd") LocalDateTime currEnd,
            @Param("areaId") Long areaId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId,
            @Param("standardKpiId") Long standardKpiId);
}
