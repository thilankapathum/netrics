package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.CellMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CellMappingRepository extends JpaRepository<CellMapping, Long> {

    Optional<CellMapping> findByPreviousCell_Id(Long previousCellId);

    Optional<CellMapping> findByNewCell_Id(Long newCellId);

    Optional<CellMapping> findByNewCell_CellName(String cellName);

    boolean existsByPreviousCell_Id(Long previousCellId);

    boolean existsByNewCell_Id(Long newCellId);
}
