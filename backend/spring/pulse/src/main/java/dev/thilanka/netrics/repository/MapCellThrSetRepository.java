package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.MapCellThrSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MapCellThrSetRepository extends JpaRepository<MapCellThrSet, Long> {

    @Query(value = """
            SELECT * FROM public.map_cell_thr_sets mcts
            WHERE mcts.standard_kpi_id = :standardKpiId
                AND mcts.rat_id = :ratId
                AND mcts.granularity_id = :granularityId
                AND mcts.is_admin = true
                AND mcts.is_active = true
                AND mcts.is_deleted = false;
            """, nativeQuery = true)
    Optional<MapCellThrSet> findAdminThrSet(@Param("standardKpiId") Long standardKpiId, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId);

    @Query(value = """
            SELECT * FROM public.map_cell_thr_sets mcts
            WHERE mcts.standard_kpi_id = :standardKpiId
                AND mcts.rat_id = :ratId
                AND mcts.granularity_id = :granularityId
                AND mcts.is_admin = false
                AND user_id = :userId
                AND mcts.is_active = true
                AND mcts.is_deleted = false;
            """, nativeQuery = true)
    Optional<MapCellThrSet> findUserThrSet(@Param("standardKpiId") Long standardKpiId, @Param("ratId") Long ratId, @Param("granularityId") Long granularityId, @Param("userId") String userId);

    Optional<MapCellThrSet> findThrSetById(Long id);

}
