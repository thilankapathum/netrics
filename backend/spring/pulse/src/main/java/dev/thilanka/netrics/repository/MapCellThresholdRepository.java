package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.MapCellThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MapCellThresholdRepository extends JpaRepository<MapCellThreshold, Long> {

    List<MapCellThreshold> findByMapCellThrSetId(Long id);
}
