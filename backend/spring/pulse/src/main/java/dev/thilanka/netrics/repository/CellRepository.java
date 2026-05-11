package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.CellDto;
import dev.thilanka.netrics.dto.CellNameDto;
import dev.thilanka.netrics.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CellRepository extends JpaRepository<Cell, Long> {

    Optional<Cell> findByCellName(String cellName);

    //TODO: Update queries to include new fields

    @Query(value = """
            SELECT *
            FROM cells
            WHERE
            	site_id IS NULL
            	OR node_name IS NULL
            	OR band_id IS NULL
            	OR rat_id IS NULL
            	OR azimuth IS NULL
            	OR beamwidth IS NULL
            	OR carrier_id IS NULL
            	OR sector_id IS NULL
            ORDER BY cell_name ASC;
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
            	OR azimuth IS NULL
            	OR beamwidth IS NULL
            	OR carrier_id IS NULL
            	OR sector_id IS NULL;
            """, nativeQuery = true)
    Integer findCellCountWithMissingInfo();

    @Query(value = """
            SELECT c.cell_name, c.node_name, r.label AS rat_name, s.site_code, b.name AS band_name,
            c.azimuth, c.beamwidth, c.is_multi_beam, carr.name AS carrier_name, sec.name AS sector_name
            FROM public.cells c
            	LEFT JOIN rat r ON c.rat_id = r.id
            	LEFT JOIN sites s ON c.site_id = s.id
            	LEFT JOIN bands b ON c.band_id = b.id
            	LEFT JOIN carriers carr ON c.carrier_id = carr.id
            	LEFT JOIN sectors sec ON c.sector_id = sec.id
            ORDER BY c.cell_name ASC;
            """, nativeQuery = true)
    List<CellDto> findAllCells();

    @Query(value = """
            SELECT c.cell_name, r.name AS rat_name, r.label AS rat_label
            FROM public.cells c
                JOIN sectors s ON s.id = c.sector_id
                JOIN rat r ON r.id = c.rat_id
            WHERE sector_id = :sectorId
                AND rat_id = :ratId
            ORDER BY c.cell_name LIMIT 20
            """, nativeQuery = true)
    List<CellNameDto> findCellsBySector(@Param("sectorId") Long sectorId, @Param("ratId") Long ratId);
}
