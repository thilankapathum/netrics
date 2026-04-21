package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByName(String name);

    Optional<Sector> findBySiteIdAndSectorIndex(Long siteId, Integer sectorIndex);

    @Query(value = """
                SELECT * FROM public.sectors
                WHERE sector_index IS NULL
                	OR name IS NULL
                	OR azimuth IS NULL
                	OR site_id IS NULL
            """, nativeQuery = true)
    List<Sector> findSectorsWithMissingInfo();
}
