package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.InfraType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InfraTypeRepository extends JpaRepository<InfraType, Long> {

    @Query(value = """
                SELECT * FROM infra_types
                WHERE infra_type = :infraType
                    AND leg_type = :legType;
            """, nativeQuery = true)
    Optional<InfraType> findByInfraAndLeg(
            @Param("infraType") String infraType,
            @Param("legType") String legType);
}
