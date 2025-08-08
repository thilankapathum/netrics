package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddKpiMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LteFddKpiMappingRepository extends JpaRepository<LteFddKpiMapping, Long> {
}
