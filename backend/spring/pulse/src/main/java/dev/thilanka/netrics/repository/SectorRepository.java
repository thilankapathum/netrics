package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByName(String name);

    Optional<Sector> findBySiteIdAndSectorIndex(Long siteId, Integer sectorIndex);
}
