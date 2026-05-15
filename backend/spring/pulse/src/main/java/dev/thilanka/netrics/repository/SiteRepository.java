package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Site;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findBySiteCode(String siteCode);

    @Query(value = """
                SELECT * FROM public.sites
                WHERE site_code IS NULL
                	OR site_name IS NULL
                	OR latitude IS NULL
                	OR longitude IS NULL
            """, nativeQuery = true)
    List<Site> findSitesWithMissingInfo();

    @Query(value = """
                SELECT COUNT(*) FROM public.sites
                WHERE site_code IS NULL
                	OR site_name IS NULL
                	OR latitude IS NULL
                	OR longitude IS NULL;
            """, nativeQuery = true)
    Integer findSiteCountWithMissingInfo();

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO sites (site_code, site_name, latitude, longitude,
                                       created_at, last_modified_at,created_by, last_modified_by)
                    VALUES (:siteCode, :siteName, :latitude, :longitude,
                            :createdAt, :modifiedAt ,:createdBy, :modifiedBy)
                    ON CONFLICT (site_code) DO UPDATE SET
                        site_name  = EXCLUDED.site_name,
                        latitude   = EXCLUDED.latitude,
                        longitude  = EXCLUDED.longitude
            """, nativeQuery = true)
    void upsert(
            @Param("siteCode") String siteCode,
            @Param("siteName") String siteName,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("modifiedAt") LocalDateTime modifiedAt,
            @Param("createdBy") String createdBy,
            @Param("modifiedBy") String modifiedBy
    );

    void deleteBySiteCode(String siteCode);
}
