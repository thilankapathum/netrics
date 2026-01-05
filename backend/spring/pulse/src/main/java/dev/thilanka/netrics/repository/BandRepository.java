package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Band;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BandRepository extends JpaRepository<Band, Long> {

    Optional<Band> findByName(String name);

    Optional<Band> findByNumber(int number);

    @Query(value = """
            SELECT b.*
            FROM bands b
            WHERE EXISTS (
                SELECT 1
                FROM cells c
                WHERE c.band_id = b.id
                  AND c.rat_id = :ratId
            );
            """, nativeQuery = true)
    List<Band> findBandsByRat(@Param("ratId") Long ratId);
}
