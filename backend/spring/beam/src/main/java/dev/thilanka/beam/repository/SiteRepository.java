package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.Site;
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

    List<Site> findAllByOrderBySiteCodeAsc();

    @Query(value = """
                SELECT * FROM public.sites
                WHERE site_code IS NULL
                	OR site_name IS NULL
                	OR latitude IS NULL
                	OR longitude IS NULL
                    OR building_height IS NULL
                    OR tower_height IS NULL
                    OR operator_id IS NULL
                    OR infra_type_id IS NULL
                ORDER BY site_code;
            """, nativeQuery = true)
    List<Site> findSitesWithMissingInfo();

    @Query(value = """
                SELECT COUNT(*) FROM public.sites
                WHERE site_code IS NULL
                	OR site_name IS NULL
                	OR latitude IS NULL
                	OR longitude IS NULL
                    OR building_height IS NULL
                    OR tower_height IS NULL
                    OR operator_id IS NULL
                    OR infra_type_id IS NULL;
            """, nativeQuery = true)
    Integer findSiteCountWithMissingInfo();

//    @Modifying
//    @Transactional
//    @Query(value = """
//            INSERT INTO sites (site_code, site_name, latitude, longitude,
//                                       created_at, last_modified_at,created_by, last_modified_by)
//                    VALUES (:siteCode, :siteName, :latitude, :longitude,
//                            :createdAt, :modifiedAt ,:createdBy, :modifiedBy)
//                    ON CONFLICT (id) DO UPDATE SET
//                        site_code  = EXCLUDED.site_code,
//                        site_name  = EXCLUDED.site_name,
//                        latitude   = EXCLUDED.latitude,
//                        longitude  = EXCLUDED.longitude
//            """, nativeQuery = true)
//    void upsert(
//            @Param("id") Long id,
//            @Param("siteCode") String siteCode,
//            @Param("siteName") String siteName,
//            @Param("latitude") Double latitude,
//            @Param("longitude") Double longitude,
//            @Param("createdAt") LocalDateTime createdAt,
//            @Param("modifiedAt") LocalDateTime modifiedAt,
//            @Param("createdBy") String createdBy,
//            @Param("modifiedBy") String modifiedBy
//    );
}
