package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.KpiMappingToOss;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LteFddKpiMappingRepository extends JpaRepository<KpiMappingToOss, Long> {

    List<KpiMappingToOss> findByRatId(Long ratId);
}
