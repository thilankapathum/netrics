package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.WorstCellsDashboardDto;
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
