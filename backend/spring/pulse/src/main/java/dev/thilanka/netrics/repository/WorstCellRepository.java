package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.WorstCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


//    @Query(value = """
//            SELECT * FROM public.worst_cells
//            WHERE period = :period
//            	AND timestamp = :timestamp
//            	AND rat_id = :ratId
//            	AND standard_kpi_id = :standardKpiId
//            	AND area_aggregation = :areaAggregation
//            """, nativeQuery = true)
//    List<WorstCell> findWorstCellsByKpi(@Param("period") String period, @Param("timestamp") LocalDateTime timestamp, @Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId, @Param("areaAggregation") String areaAggregation);

    @Query(value = """
            SELECT * FROM public.worst_cells
            WHERE period = :period
            	AND timestamp = :timestamp
            	AND rat_id = :ratId
            	AND standard_kpi_id = :standardKpiId
            	AND area_id = :areaId
            """, nativeQuery = true)
    List<WorstCell> findWorstCellsByKpi(@Param("period") String period, @Param("timestamp") LocalDateTime timestamp, @Param("ratId") Long ratId, @Param("standardKpiId") Long standardKpiId, @Param("areaId") Long areaId);
}
