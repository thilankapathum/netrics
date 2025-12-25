package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Band;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BandRepository extends JpaRepository<Band, Long> {

    Optional<Band> findByName(String name);

    Optional<Band> findByNumber(int number);
}
