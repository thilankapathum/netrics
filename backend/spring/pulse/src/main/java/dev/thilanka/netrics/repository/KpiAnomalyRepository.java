package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.BreachingCellProjection;
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
            WHERE kv.timestamp = :timestamp
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
    List<BreachingCellProjection> findBreachingCells(@Param("timestamp") LocalDateTime timestamp,
                                                     @Param("ratId") Long ratId,
                                                     @Param("granularityId") Long granularityId);

    @Modifying
    @Query(value = """
            WITH pairs AS (
                SELECT * FROM unnest(:cellNames ::varchar[], :kpiIds ::bigint[])
                    AS p(cell_name, standard_kpi_id)
            ),
            current_point AS (
                SELECT
                    pairs.cell_name, pairs.standard_kpi_id, sk.worst_order,
                    COALESCE(
                        CASE WHEN sk.unit = '%' THEN (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) * 100
                             ELSE (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) END,
                        kv.kpi_value) AS observed_value
                FROM pairs
                JOIN kpi_values kv
                    ON kv.cell_name = pairs.cell_name AND kv.standard_kpi_id = pairs.standard_kpi_id
                   AND kv.rat_id = :ratId AND kv.granularity_id = :granularityId AND kv.timestamp = :timestamp
                JOIN standard_kpi sk ON sk.id = pairs.standard_kpi_id
            ),
            history AS (
                SELECT
                    cp.cell_name, cp.standard_kpi_id,
                    COALESCE(
                        CASE WHEN sk.unit = '%' THEN (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) * 100
                             ELSE (kv.numerator_kpi_value / NULLIF(kv.denominator_kpi_value,0)) END,
                        kv.kpi_value) AS hist_value
                FROM current_point cp
                JOIN kpi_values kv
                    ON kv.cell_name = cp.cell_name AND kv.standard_kpi_id = cp.standard_kpi_id
                   AND kv.rat_id = :ratId AND kv.granularity_id = :granularityId
                   AND kv.timestamp < :timestamp AND kv.timestamp >= :timestamp ::timestamp - (:baselineDays * INTERVAL '1 day')
                JOIN standard_kpi sk ON sk.id = cp.standard_kpi_id
            ),
            stats AS (
                SELECT cell_name, standard_kpi_id,
                       COUNT(*) AS n,
                       PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY hist_value) AS median_value
                FROM history GROUP BY cell_name, standard_kpi_id
            ),
            mad_calc AS (
                SELECT h.cell_name, h.standard_kpi_id, s.n, s.median_value,
                       PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ABS(h.hist_value - s.median_value)) AS mad
                FROM history h JOIN stats s ON h.cell_name = s.cell_name AND h.standard_kpi_id = s.standard_kpi_id
                GROUP BY h.cell_name, h.standard_kpi_id, s.n, s.median_value
            )
            INSERT INTO kpi_anomalies
                (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp,
                 observed_value, baseline_median, mad, robust_z_score, severity, detected_at)
            SELECT
                cp.cell_name, cp.standard_kpi_id, :ratId, :granularityId, :timestamp,
                cp.observed_value, m.median_value, m.mad,
                0.6745 * (cp.observed_value - m.median_value) / m.mad,
                CASE
                    WHEN ABS(0.6745 * (cp.observed_value - m.median_value) / m.mad) >= 6 THEN 'critical'
                    WHEN ABS(0.6745 * (cp.observed_value - m.median_value) / m.mad) >= 4 THEN 'high'
                    ELSE 'moderate'
                END,
                NOW()
            FROM current_point cp
            JOIN mad_calc m ON cp.cell_name = m.cell_name AND cp.standard_kpi_id = m.standard_kpi_id
            WHERE m.n >= :minHistory AND m.mad > 0
              AND ABS(0.6745 * (cp.observed_value - m.median_value) / m.mad) >= :zThreshold
            ON CONFLICT (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp) DO NOTHING
            """, nativeQuery = true)
    int detectAndInsertAnomalies(@Param("cellNames") String[] cellNames,
                                 @Param("kpiIds") Long[] kpiIds,
                                 @Param("ratId") Long ratId,
                                 @Param("granularityId") Long granularityId,
                                 @Param("timestamp") LocalDateTime timestamp,
                                 @Param("baselineDays") int baselineDays,
                                 @Param("minHistory") int minHistory,
                                 @Param("zThreshold") double zThreshold);
}
