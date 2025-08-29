package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.district.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District,Long> {
    Optional<District> findByName(String name);

    @Query(value = """
            SELECT * FROM districts
            ORDER BY districts.name ASC;
            """ , nativeQuery = true)
    List<District> findAllDistrictsAsc();
}
