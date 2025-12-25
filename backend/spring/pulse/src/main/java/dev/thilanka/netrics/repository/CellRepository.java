package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CellRepository extends JpaRepository<Cell, Long> {

    Optional<Cell> findByCellName(String cellName);
}
