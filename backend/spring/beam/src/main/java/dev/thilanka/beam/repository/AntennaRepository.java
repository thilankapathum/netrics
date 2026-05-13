package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.Antenna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AntennaRepository extends JpaRepository<Antenna, Long> {


    @Query(value = """
            SELECT * FROM public.antennas
            WHERE sector_id = :sectorId
            ORDER BY antenna_index ASC;
            """, nativeQuery = true)
    List<Antenna> findBySectorId(@Param("sectorId") Long sectorId);
}
