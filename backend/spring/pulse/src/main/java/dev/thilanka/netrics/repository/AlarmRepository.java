package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.AlarmsDto;
import dev.thilanka.netrics.dto.CellAlarmCorrelationProjection;
import dev.thilanka.netrics.dto.CellAlarmDto;
import dev.thilanka.netrics.entity.alarms.Alarms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarms, Long> {

    @Query(value = """
            WITH latest_alarms AS (
                SELECT DISTINCT ON (a.alarm_source_id, a.node_name, a.alarm_id) a.*
                FROM alarms a
                WHERE a.occurrence_time <= now()
                    AND a.occurrence_time >= :startTime
                ORDER BY a.alarm_source_id, a.node_name, a.alarm_id, a.occurrence_time DESC
            )
            SELECT
                a.alarm_id,
                c.cell_name,
                c.node_name,
                a.severity,
                a.occurrence_time,
                at.name          AS alarm_type,
                ad.alarm_code,
                ad.alarm_name,
                a.location,
                a.ack_state,
                a.clear_state,
                a.specific_problem,
                a.additional_info,
                alms.label AS alarm_source
            FROM latest_alarms a
            JOIN cells c
                ON a.node_name = c.node_name
            LEFT JOIN alarm_definitions ad
                ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at"
                ON at.id = a.alarm_type_id
            JOIN alarm_sources alms
                ON alms.id = a.alarm_source_id
            WHERE c.cell_name = :cellName
            ORDER BY a.occurrence_time DESC;
            """, nativeQuery = true)
    List<CellAlarmDto> getAlarmsByCell(@Param("cellName") String cellName, @Param("startTime") LocalDateTime startTime);

    @Query(value = """
            WITH latest_alarms AS (
                SELECT DISTINCT ON (a.alarm_source_id, a.node_name, a.alarm_id) a.*
                FROM alarms a
                WHERE a.occurrence_time <= :endTime
                    AND a.occurrence_time >= :startTime
                ORDER BY a.alarm_source_id, a.node_name, a.alarm_id, a.occurrence_time DESC
            )
            SELECT
                a.alarm_id,
                c.cell_name,
                c.node_name,
                a.severity,
                a.occurrence_time,
                at.name          AS alarm_type,
                ad.alarm_code,
                ad.alarm_name,
                a.location,
                a.ack_state,
                a.clear_state,
                a.specific_problem,
                a.additional_info,
                alms.label AS alarm_source
            FROM latest_alarms a
            JOIN cells c
                ON a.node_name = c.node_name
            LEFT JOIN alarm_definitions ad
                ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at"
                ON at.id = a.alarm_type_id
            JOIN alarm_sources alms
                ON alms.id = a.alarm_source_id
            WHERE c.cell_name = :cellName
            ORDER BY a.occurrence_time DESC;
            """, nativeQuery = true)
    List<CellAlarmDto> getAlarmsByCell(@Param("cellName") String cellName, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);


    @Query(value = """
            WITH latest_alarms AS (
                SELECT DISTINCT ON (a.alarm_source_id, a.node_name, a.alarm_id) a.*
                FROM alarms a
                WHERE a.occurrence_time <= now()
                    AND a.occurrence_time >= :startTime
                ORDER BY a.alarm_source_id, a.node_name, a.alarm_id, a.occurrence_time DESC
            )
            SELECT
                a.alarm_id         AS alarmId,
                a.node_name        AS nodeName,
                a.severity         AS severity,
                a.occurrence_time  AS occurrenceTime,
                at.name            AS alarmType,
                ad.alarm_code      AS alarmCode,
                ad.alarm_name      AS alarmName,
                a.location         AS location,
                a.ack_state        AS ackState,
                a.clear_state      AS clearState,
                a.specific_problem AS specificProblem,
                a.additional_info  AS additionalInfo,
                alms.label         AS alarmSource,
                a.parsed_cell_name AS parsedCellName
            FROM latest_alarms a
            LEFT JOIN alarm_definitions ad ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at" ON at.id = a.alarm_type_id
            JOIN alarm_sources alms ON alms.id = a.alarm_source_id
            WHERE (CAST(:nodeName AS text) IS NULL OR a.node_name ILIKE CONCAT('%', CAST(:nodeName AS text), '%'))
              AND (CAST(:severity AS text) IS NULL OR a.severity = CAST(:severity AS text))
              AND (CAST(:alarmType AS text) IS NULL OR at.name = CAST(:alarmType AS text))
              AND (CAST(:alarmName AS text) IS NULL OR ad.alarm_name ILIKE CONCAT('%', CAST(:alarmName AS text), '%'))
              AND (CAST(:ackState AS text) IS NULL OR a.ack_state = CAST(:ackState AS text))
              AND (CAST(:clearState AS text) IS NULL OR a.clear_state = CAST(:clearState AS text))
              AND (CAST(:alarmSource AS text) IS NULL OR alms.name = CAST(:alarmSource AS text))
              AND (:areaId IS NULL OR EXISTS (
                    SELECT 1 FROM cells c
                    JOIN district_codes dc ON dc.id = c.district_code_id
                    JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
                    WHERE c.node_name = a.node_name AND adcm.area_id = CAST(:areaId AS bigint)
              ))
            ORDER BY a.occurrence_time DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<AlarmsDto> findAlarms(
            @Param("startTime") LocalDateTime startTime,
            @Param("nodeName") String nodeName,
            @Param("severity") String severity,
            @Param("alarmType") String alarmType,
            @Param("alarmName") String alarmName,
            @Param("ackState") String ackState,
            @Param("clearState") String clearState,
            @Param("alarmSource") String alarmSource,
            @Param("areaId") Long areaId,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query(value = """
            WITH latest_alarms AS (
                SELECT DISTINCT ON (a.alarm_source_id, a.node_name, a.alarm_id) a.*
                FROM alarms a
                WHERE a.occurrence_time <= now()
                    AND a.occurrence_time >= :startTime
                ORDER BY a.alarm_source_id, a.node_name, a.alarm_id, a.occurrence_time DESC
            )
            SELECT count(*)
            FROM latest_alarms a
            LEFT JOIN alarm_definitions ad ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at" ON at.id = a.alarm_type_id
            JOIN alarm_sources alms ON alms.id = a.alarm_source_id
            WHERE (CAST(:nodeName AS text) IS NULL OR a.node_name ILIKE CONCAT('%', CAST(:nodeName AS text), '%'))
              AND (CAST(:severity AS text) IS NULL OR a.severity = CAST(:severity AS text))
              AND (CAST(:alarmType AS text) IS NULL OR at.name = CAST(:alarmType AS text))
              AND (CAST(:alarmName AS text) IS NULL OR ad.alarm_name ILIKE CONCAT('%', CAST(:alarmName AS text), '%'))
              AND (CAST(:ackState AS text) IS NULL OR a.ack_state = CAST(:ackState AS text))
              AND (CAST(:clearState AS text) IS NULL OR a.clear_state = CAST(:clearState AS text))
              AND (CAST(:alarmSource AS text) IS NULL OR alms.label = CAST(:alarmSource AS text))
              AND (:areaId IS NULL OR EXISTS (
                    SELECT 1 FROM cells c
                    JOIN district_codes dc ON dc.id = c.district_code_id
                    JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
                    WHERE c.node_name = a.node_name AND adcm.area_id = CAST(:areaId AS bigint)
              ))
            """, nativeQuery = true)
    long countAlarms(
            @Param("startTime") LocalDateTime startTime,
            @Param("nodeName") String nodeName,
            @Param("severity") String severity,
            @Param("alarmType") String alarmType,
            @Param("alarmName") String alarmName,
            @Param("ackState") String ackState,
            @Param("clearState") String clearState,
            @Param("alarmSource") String alarmSource,
            @Param("areaId") Long areaId
    );

    @Query(value = """
        WITH pairs AS (
            SELECT * FROM unnest(:cellNames ::varchar[], :timestamps ::timestamp[])
                AS p(cell_name, obs_timestamp)
        ),
        targets AS (
            SELECT
                p.cell_name,
                p.obs_timestamp                                            AS window_start,
                p.obs_timestamp + (g.window_seconds || ' seconds')::interval AS window_end
            FROM pairs p
            CROSS JOIN granularity g
            WHERE g.id = :granularityId
        ),
        candidate_matches AS (
            SELECT
                t.cell_name,
                al.id AS alarm_id,
                al.alarm_definition_id,
                'CELL' AS match_level
            FROM targets t
            JOIN alarms al ON al.parsed_cell_name = t.cell_name
            WHERE al.occurrence_time >= t.window_start
              AND al.occurrence_time <  t.window_end

            UNION ALL

            SELECT
                t.cell_name,
                al.id AS alarm_id,
                al.alarm_definition_id,
                'NODE' AS match_level
            FROM targets t
            JOIN cells c   ON c.cell_name = t.cell_name
            JOIN alarms al ON al.node_name = c.node_name
            WHERE al.occurrence_time >= t.window_start
              AND al.occurrence_time <  t.window_end
        ),
        deduped AS (
            SELECT DISTINCT ON (cell_name, alarm_id) *
            FROM candidate_matches
            ORDER BY cell_name, alarm_id,
                     CASE match_level WHEN 'CELL' THEN 0 ELSE 1 END
        )
        SELECT
            cell_name                                  AS cellName,
            TRUE                                        AS hasAlarmCorrelation,
            COUNT(DISTINCT alarm_definition_id)::int    AS distinctAlarmDefCount,
            COUNT(*)::int                               AS totalAlarmOccurrences,
            MIN(match_level)                            AS bestMatchLevel
        FROM deduped
        GROUP BY cell_name
        """, nativeQuery = true)
    List<CellAlarmCorrelationProjection> findLiveAlarmCorrelationsForCells(
            @Param("cellNames") String[] cellNames,
            @Param("timestamps") LocalDateTime[] timestamps,
            @Param("granularityId") Long granularityId);
}
