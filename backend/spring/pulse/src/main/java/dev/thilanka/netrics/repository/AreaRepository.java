package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.AreaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area,Long> {

    Optional<Area> findByName(String name);

    List<Area> findByAreaType(AreaType areaType);

    @Query(value = """
            SELECT * FROM public.areas
            WHERE area_type_id = :areaTypeId
            	AND enabled = true
            ORDER BY name ASC;
            """, nativeQuery = true)
    List<Area> findByAreaTypeEnabled(@Param("areaTypeId") Long areaTypeId);
}
