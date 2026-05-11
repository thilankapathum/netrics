package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, Integer> {
    Optional<Carrier> findByName(String name);
}
