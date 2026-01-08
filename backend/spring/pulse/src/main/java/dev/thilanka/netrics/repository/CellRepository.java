package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CellRepository extends JpaRepository<Cell, Long> {

    Optional<Cell> findByCellName(String cellName);

    @Query(value = """
            SELECT *
            FROM cells
            WHERE
            	site_id IS NULL
            	OR node_name IS NULL
            	OR band_id IS NULL
            	OR rat_id IS NULL
            """, nativeQuery = true)
    List<Cell> findCellsWithMissingInfo();

    @Query(value = """
            SELECT COUNT(*)
            FROM cells
            WHERE
            	site_id IS NULL
            	OR node_name IS NULL
            	OR band_id IS NULL
            	OR rat_id IS NULL
            """, nativeQuery = true)
    Integer findCellCountWithMissingInfo();
}
