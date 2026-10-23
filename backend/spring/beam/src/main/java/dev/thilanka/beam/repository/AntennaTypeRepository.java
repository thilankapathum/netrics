package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.AntennaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AntennaTypeRepository extends JpaRepository<AntennaType, Long> {
    Optional<AntennaType> findByName(String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM antenna_types WHERE id = :id", nativeQuery = true)
    void deleteByIdNative(@Param("id") Long id);
}
