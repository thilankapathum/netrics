package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.WorstCellsDashboardDto;
import dev.thilanka.netrics.dto.WorstCellsWithLatestDto;
import dev.thilanka.netrics.entity.WorstCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorstCellRepository extends JpaRepository<WorstCell, Long> {

    @Query(value = """
            SELECT * FROM public.worst_cells
            WHERE cell_name = :cellName
            	AND period = :period
            	AND timestamp = :timestamp
            	AND rat_id = :ratId
            	AND standard_kpi_id = :standardKpiId
            	AND area_id = :areaId
            """, nativeQuery = true)
    Optional<WorstCell> findWorstCellByCellName(@Param("cellName") String cellName, @Param("period") String period, @Param("timestamp") LocalDateTime timestamp, @Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId, @Param("areaId") Long areaId);


    @Query(value = """
            SELECT worst_cells.id id, cell_name, sk.kpi_name kpi_name, sk.label kpi_label, worst_cells.unit unit, value, previous_value, difference, improved
            FROM public.worst_cells
            LEFT JOIN public.lte_fdd_standard_kpi sk ON standard_kpi_id = sk.id
            WHERE period = :period
            	AND timestamp = :timestamp
            	AND worst_cells.rat_id = :ratId
            	AND standard_kpi_id = :standardKpiId
            	AND area_id = :areaId
            """, nativeQuery = true)
    List<WorstCellsDashboardDto> findWorstCellsByKpi(@Param("period") String period, @Param("timestamp") LocalDateTime timestamp, @Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId, @Param("areaId") Long areaId);


    @Query(value = """
            SELECT\s
                worst_cells.id AS id,
                worst_cells.cell_name,
                sk.kpi_name AS kpi_name,
                sk.label AS kpi_label,
                worst_cells.unit AS unit,
                worst_cells.value,
                worst_cells.previous_value,
                worst_cells.difference,
                worst_cells.improved,
                kd.kpi_value AS latest_value,
            	kd.kpi_value - worst_cells.value AS latest_difference,
            	CASE
            		WHEN sk.worst_order = 'ASC'  AND (kd.kpi_value - value) > 0 THEN true
            		WHEN sk.worst_order = 'DESC' AND (kd.kpi_value - value) < 0 THEN true
            		ELSE false
            	END AS latest_improved
            FROM public.worst_cells
            LEFT JOIN public.lte_fdd_standard_kpi sk\s
                ON worst_cells.standard_kpi_id = sk.id
            LEFT JOIN public.lte_fdd_kpi_day kd
                ON kd.cell_name = worst_cells.cell_name
                AND kd.timestamp = :latestDate
                AND kd.lte_fdd_standard_kpi_id = worst_cells.standard_kpi_id
                AND kd.rat_id = worst_cells.rat_id
            WHERE worst_cells.period = :period
            	AND worst_cells.timestamp = :timestamp
            	AND worst_cells.rat_id = :ratId
            	AND worst_cells.standard_kpi_id = :standardKpiId
            	AND worst_cells.area_id = :areaId
            """, nativeQuery = true)
    List<WorstCellsWithLatestDto> findWorstCellsByKpi(@Param("period") String period, @Param("timestamp") LocalDateTime timestamp, @Param("latestDate") LocalDateTime latestDate , @Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId, @Param("areaId") Long areaId);


    @Query(value = """
            SELECT DISTINCT	timestamp
            FROM public.worst_cells
            WHERE period = :period
            	AND area_id = :areaId
            	AND rat_id = :ratId
            	AND standard_kpi_id = :standardKpiId
            ORDER BY timestamp DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Timestamp> findTimestamps(@Param("period") String period,@Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId, @Param("areaId") Long areaId);
}
