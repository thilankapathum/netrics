package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Rat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatRepository extends JpaRepository<Rat, Long> {
    Optional<Rat> findByLabel(String label);
    Optional<Rat> findByName(String name);
}
