package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.AreaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area,Long> {

    Optional<Area> findByName(String name);

    List<Area> findByAreaType(AreaType areaType);
}
