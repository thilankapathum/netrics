package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.entity.Sector;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query(value = """
            SELECT COUNT(*)
            FROM sectors
            WHERE
                azimuth IS NULL
                OR site_id IS NULL;
            """, nativeQuery = true)
    Integer findSectorCountWithMissingInfo();

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO sectors (sector_index, name, azimuth, site_id,
                                 created_at, last_modified_at, created_by, last_modified_by)
            VALUES (:sectorIndex, :name, :azimuth, :siteId,
                    :createdAt, :modifiedAt ,:createdBy, :modifiedBy)
            ON CONFLICT (name) DO UPDATE SET
                sector_index = EXCLUDED.sector_index,
                azimuth      = EXCLUDED.azimuth,
                site_id      = EXCLUDED.site_id
                -- Beam-only columns (antenna details, tilt, etc.)
                -- are intentionally absent
            """, nativeQuery = true)
    void upsert(
            @Param("sectorIndex") Integer sectorIndex,
            @Param("name") String name,
            @Param("azimuth") Integer azimuth,
            @Param("siteId") Long siteId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("modifiedAt") LocalDateTime modifiedAt,
            @Param("createdBy") String createdBy,
            @Param("modifiedBy") String modifiedBy
    );

    void deleteByName(String name);
}
