package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.Antenna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AntennaRepository extends JpaRepository<Antenna, Long> {


    @Query(value = """
            SELECT * FROM public.antennas
            WHERE sector_id = :sectorId
            ORDER BY antenna_index ASC;
            """, nativeQuery = true)
    List<Antenna> findBySectorId(@Param("sectorId") Long sectorId);

    List<Antenna> findByAntennaType_Id(Long antennaTypeId);

    List<Antenna> findByManufacturer_Id(Long manufacturerId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE antennas SET antenna_type_id = NULL WHERE antenna_type_id = :id", nativeQuery = true)
    void nullifyAntennaType(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE antennas SET manufacturer_id = NULL WHERE manufacturer_id = :id", nativeQuery = true)
    void nullifyManufacturer(@Param("id") Long id);
}
