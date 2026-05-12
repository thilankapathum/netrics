package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.BeamSite;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BeamSiteRepository extends JpaRepository<BeamSite, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO sites (id, site_code, site_name, latitude, longitude,
                                       created_at, created_by)
                    VALUES (:id, :siteCode, :siteName, :latitude, :longitude,
                            :createdAt, :createdBy)
                    ON CONFLICT (id) DO UPDATE SET
                        site_code  = EXCLUDED.site_code,
                        site_name  = EXCLUDED.site_name,
                        latitude   = EXCLUDED.latitude,
                        longitude  = EXCLUDED.longitude
            """, nativeQuery = true)
    void upsert(
            @Param("id") Long id,
            @Param("siteCode") String siteCode,
            @Param("siteName") String siteName,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("createdBy") String createdBy
    );
}
