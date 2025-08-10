package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LteFddBasicKpiRepository extends JpaRepository<LteFddBasicKpi,Long> {
    Optional<LteFddBasicKpi> findByKpiName(String s);
}
