package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Granularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GranularityRepository extends JpaRepository<Granularity,Long> {

    Optional<Granularity> findByName(String name);

    Optional<Granularity> findById(Long id);
}
