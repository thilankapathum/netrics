package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.AreaDistrictCodeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AreaDistrictCodeMappingRepository extends JpaRepository<AreaDistrictCodeMapping,Long> {

    @Query("SELECT m.districtCode.id FROM AreaDistrictCodeMapping m WHERE m.area.name = :areaName")
    Set<Long> findDistrictCodeIdsByAreaName(@Param("areaName") String areaName);
}
