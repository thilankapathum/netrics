package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LteFddStandardKpiRepository extends JpaRepository<LteFddStandardKpi, Long> {
    Optional<LteFddStandardKpi> findByKpiName(String kpiName);
}
