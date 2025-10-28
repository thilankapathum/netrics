package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.district.DistrictCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DistrictCodeRepository extends JpaRepository<DistrictCode,Long> {

    @Query(value = """
            SELECT * FROM district_codes
            WHERE :cellName LIKE district_codes.code || '%'
            ORDER BY LENGTH(district_codes.code) DESC
            """, nativeQuery = true)
    Optional<DistrictCode> findDistrictCodeByPrefix(@Param("cellName") String cellName);
}
