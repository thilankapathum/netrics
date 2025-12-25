package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findBySiteCode(String siteCode);
}
