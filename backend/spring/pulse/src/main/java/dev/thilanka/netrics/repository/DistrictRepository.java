package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.district.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District,Long> {
    Optional<District> findByName(String name);
}
