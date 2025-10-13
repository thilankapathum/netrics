package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.StandardRawKpiMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandardRawKpiMappingRepository extends JpaRepository<StandardRawKpiMapping, Long> {

    List<StandardRawKpiMapping> findByRat(Rat rat);
}
