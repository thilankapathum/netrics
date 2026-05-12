package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.BeamBand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeamBandRepository extends JpaRepository<BeamBand, Long> {
}
