package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddKpiMappingToOss;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LteFddKpiMappingRepository extends JpaRepository<LteFddKpiMappingToOss, Long> {

    List<LteFddKpiMappingToOss> findByRatId(Long ratId);
}
