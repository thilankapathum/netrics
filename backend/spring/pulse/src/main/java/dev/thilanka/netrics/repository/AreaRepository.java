package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area,Long> {

    Optional<Area> findByName(String name);
}
