package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.BeamBand;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BeamBandRepository extends JpaRepository<BeamBand, Long> {
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO bands (id, name, number, unit,
                               created_at, created_by)
            VALUES (:id, :name, :number, :unit,
                    :createdAt, :createdBy)
            ON CONFLICT (id) DO UPDATE SET
                name   = EXCLUDED.name,
                number = EXCLUDED.number,
                unit   = EXCLUDED.unit
            """, nativeQuery = true)
    void upsert(
            @Param("id")        Long id,
            @Param("name")      String name,
            @Param("number")    int number,
            @Param("unit")      String unit,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("createdBy") String createdBy
    );

    Optional<BeamBand> findByName(String name);
}
