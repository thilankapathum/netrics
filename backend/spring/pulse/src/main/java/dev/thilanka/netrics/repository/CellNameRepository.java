package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.CellName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CellNameRepository extends JpaRepository<CellName, Long> {

    /* DEPRECATED */

    Optional<CellName> findByCellName(String cellName);
}
