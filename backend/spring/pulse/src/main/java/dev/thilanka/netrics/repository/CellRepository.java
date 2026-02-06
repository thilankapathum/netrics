package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.CellDto;
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

    @Query(value = """
            SELECT c.cell_name, c.node_name, r.label AS rat_name, s.site_code, b.name AS band_name FROM public.cells c
            LEFT JOIN rat r ON c.rat_id = r.id
            LEFT JOIN sites s ON c.site_id = s.id
            LEFT JOIN bands b ON c.band_id = b.id
            ORDER BY c.cell_name ASC;
            """, nativeQuery = true)
    List<CellDto> findAllCells();
}
