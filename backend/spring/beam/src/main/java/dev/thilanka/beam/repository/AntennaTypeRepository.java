package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.AntennaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AntennaTypeRepository extends JpaRepository<AntennaType, Long> {
    Optional<AntennaType> findByName(String name);
}
