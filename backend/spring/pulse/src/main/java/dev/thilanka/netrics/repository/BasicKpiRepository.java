package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.BasicKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BasicKpiRepository extends JpaRepository<BasicKpi,Long> {

    Optional<BasicKpi> findByKpiNameAndRat(String kpiName, Rat rat);

    List<BasicKpi> findByRatId(Long ratId);

}
