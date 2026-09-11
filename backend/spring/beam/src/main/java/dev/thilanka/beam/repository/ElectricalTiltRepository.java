package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.ElectricalTilt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElectricalTiltRepository extends JpaRepository<ElectricalTilt,Long> {
    List<ElectricalTilt> findByAntennaId(Long antennaId);
}
