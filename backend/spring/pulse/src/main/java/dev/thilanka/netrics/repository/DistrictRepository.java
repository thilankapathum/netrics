package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.district.District;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District,Long> {
}
