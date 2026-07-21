package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.AlarmsDto;
import dev.thilanka.netrics.dto.CellAlarmDto;
import dev.thilanka.netrics.entity.alarms.Alarms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarms, Long> {

    @Query(value = """
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
            FROM alarms a
            JOIN cells c
                ON a.node_name = c.node_name
            LEFT JOIN alarm_definitions ad
                ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at"
                ON at.id = a.alarm_type_id
            JOIN alarm_sources alms
                ON alms.id = a.alarm_source_id
            WHERE c.cell_name = :cellName
                AND a.occurrence_time <= now()
                AND a.occurrence_time >= :startTime
            ORDER BY a.occurrence_time DESC;
            """, nativeQuery = true)
    List<CellAlarmDto> getAlarmsByCell(@Param("cellName") String cellName, @Param("startTime") LocalDateTime startTime);


    @Query(value = """
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
                alms.label         AS alarmSource
            FROM alarms a
            LEFT JOIN alarm_definitions ad ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at" ON at.id = a.alarm_type_id
            JOIN alarm_sources alms ON alms.id = a.alarm_source_id
            WHERE a.occurrence_time <= now()
              AND a.occurrence_time >= :startTime
              AND (CAST(:nodeName AS text) IS NULL OR a.node_name ILIKE CONCAT('%', CAST(:nodeName AS text), '%'))
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
            SELECT count(*)
            FROM alarms a
            LEFT JOIN alarm_definitions ad ON ad.id = a.alarm_definition_id
            JOIN alarm_types "at" ON at.id = a.alarm_type_id
            JOIN alarm_sources alms ON alms.id = a.alarm_source_id
            WHERE a.occurrence_time <= now()
              AND a.occurrence_time >= :startTime
              AND (CAST(:nodeName AS text) IS NULL OR a.node_name ILIKE CONCAT('%', CAST(:nodeName AS text), '%'))
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
}
