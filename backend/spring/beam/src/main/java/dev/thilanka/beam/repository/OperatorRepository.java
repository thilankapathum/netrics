package dev.thilanka.beam.repository;

import dev.thilanka.beam.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperatorRepository extends JpaRepository<Operator,Long> {
    Optional<Operator> findByName(String name);
}
