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
}
