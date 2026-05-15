package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.Sector;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {

//    @Modifying
//    @Transactional
//    @Query(value = """
//            INSERT INTO sectors (id, sector_index, name, azimuth, site_id,
//                                 created_at, created_by)
//            VALUES (:id, :sectorIndex, :name, :azimuth, :siteId,
//                    :createdAt, :createdBy)
//            ON CONFLICT (id) DO UPDATE SET
//                sector_index = EXCLUDED.sector_index,
//                name         = EXCLUDED.name,
//                azimuth      = EXCLUDED.azimuth,
//                site_id      = EXCLUDED.site_id
//                -- Beam-only columns (antenna details, tilt, etc.)
//                -- are intentionally absent
//            """, nativeQuery = true)
//    void upsert(
//            @Param("id") Long id,
//            @Param("sectorIndex") Integer sectorIndex,
//            @Param("name") String name,
//            @Param("azimuth") Integer azimuth,
//            @Param("siteId") Long siteId,
//            @Param("createdAt") LocalDateTime createdAt,
//            @Param("createdBy") String createdBy
//    );

    Optional<Sector> findByName(String name);

}
