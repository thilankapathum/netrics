package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.anomaly.AnomalyAlarmCorrelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AnomalyAlarmCorrelationRepository extends JpaRepository<AnomalyAlarmCorrelation, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        WITH target_anomalies AS (
            SELECT
                ka.id AS anomaly_id,
                ka.timestamp AS anomaly_timestamp,
                ka.cell_name,
                ka.timestamp AS window_start,
                ka.timestamp + (g.window_seconds || ' seconds')::interval AS window_end,
                g.window_seconds
            FROM kpi_anomalies ka
            JOIN granularity g ON g.id = ka.granularity_id
            WHERE ka.detected_at >= now() - (:lookbackDays || ' days')::interval
        ),
        candidate_matches AS (
            -- Tier 2: CELL-level match
            SELECT
                ta.anomaly_id, ta.anomaly_timestamp,
                al.id AS alarm_id, al.alarm_definition_id, al.alarm_source_id,
                'CELL' AS match_level,
                GREATEST(0, EXTRACT(EPOCH FROM (
                    LEAST(COALESCE(al.clear_time, ta.window_end), ta.window_end) - al.occurrence_time
                )))::int AS overlap_seconds,
                ta.window_seconds
            FROM target_anomalies ta
            JOIN alarms al ON al.parsed_cell_name = ta.cell_name
            WHERE al.occurrence_time >= ta.window_start
              AND al.occurrence_time <  ta.window_end
 
            UNION ALL
 
            -- Tier 1: NODE-level match (all vendors)
            SELECT
                ta.anomaly_id, ta.anomaly_timestamp,
                al.id AS alarm_id, al.alarm_definition_id, al.alarm_source_id,
                'NODE' AS match_level,
                GREATEST(0, EXTRACT(EPOCH FROM (
                    LEAST(COALESCE(al.clear_time, ta.window_end), ta.window_end) - al.occurrence_time
                )))::int AS overlap_seconds,
                ta.window_seconds
            FROM target_anomalies ta
            JOIN cells c   ON c.cell_name = ta.cell_name
            JOIN alarms al ON al.node_name = c.node_name
            WHERE al.occurrence_time >= ta.window_start
              AND al.occurrence_time <  ta.window_end
        ),
        deduped AS (
            -- If the same alarm row matched at both CELL and NODE tier for the
            -- same anomaly, keep the more precise CELL match.
            SELECT DISTINCT ON (anomaly_id, alarm_id) *
            FROM candidate_matches
            ORDER BY anomaly_id, alarm_id,
                     CASE match_level WHEN 'CELL' THEN 0 ELSE 1 END
        )
        INSERT INTO anomaly_alarm_correlations
            (anomaly_id, anomaly_timestamp, alarm_id, alarm_definition_id, alarm_source_id,
             match_level, overlap_seconds, window_seconds, created_at)
        SELECT anomaly_id, anomaly_timestamp, alarm_id, alarm_definition_id, alarm_source_id,
               match_level, overlap_seconds, window_seconds, now()
        FROM deduped
        ON CONFLICT (anomaly_timestamp, anomaly_id, alarm_id) DO NOTHING
        """, nativeQuery = true)
    int insertCorrelations(@Param("lookbackDays") int lookbackDays);


    @Modifying
    @Transactional
    @Query(value = """
        WITH rollup AS (
            SELECT
                aac.anomaly_id,
                ka2.timestamp                                   AS anomaly_timestamp,
                COUNT(*)                                        AS total_occurrences,
                COUNT(DISTINCT aac.alarm_definition_id)          AS distinct_defs,
                MIN(aac.match_level)                             AS best_match_level  -- 'CELL' < 'NODE' alphabetically
            FROM anomaly_alarm_correlations aac
            JOIN kpi_anomalies ka2
                ON ka2.id = aac.anomaly_id
               AND ka2.timestamp = aac.anomaly_timestamp
            WHERE ka2.detected_at >= now() - (:lookbackDays || ' days')::interval
            GROUP BY aac.anomaly_id, ka2.timestamp
        )
        UPDATE kpi_anomalies ka
        SET has_alarm_correlation    = TRUE,
            total_alarm_occurrences  = r.total_occurrences,
            distinct_alarm_def_count = r.distinct_defs,
            best_match_level         = r.best_match_level
        FROM rollup r
        WHERE ka.id = r.anomaly_id
          AND ka.timestamp = r.anomaly_timestamp
        """, nativeQuery = true)
    int refreshRollups(@Param("lookbackDays") int lookbackDays);
}
