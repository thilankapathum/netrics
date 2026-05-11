package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
