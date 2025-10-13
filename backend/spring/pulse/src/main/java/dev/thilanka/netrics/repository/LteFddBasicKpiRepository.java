package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LteFddBasicKpiRepository extends JpaRepository<LteFddBasicKpi,Long> {
//    Optional<LteFddBasicKpi> findByKpiName(String s);


    @Query(value = """
            SELECT * FROM lte_fdd_basic_kpi
            WHERE rat_id = :ratId
            """, nativeQuery = true)
    Optional<LteFddBasicKpi> findByKpiName(@Param("kpiName") String kpiName, @Param("ratId") Long ratId);

    List<LteFddBasicKpi> findByRatId(Long ratId);

}
