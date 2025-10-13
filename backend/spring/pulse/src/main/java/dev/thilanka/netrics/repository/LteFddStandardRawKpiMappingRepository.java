package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardRawKpiMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LteFddStandardRawKpiMappingRepository extends JpaRepository<LteFddStandardRawKpiMapping, Long> {

    List<LteFddStandardRawKpiMapping> findByRat(Rat rat);
}
