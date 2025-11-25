package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.AreaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaTypeRepository extends JpaRepository<AreaType,Long> {

    Optional<AreaType> findByName(String name);
}
