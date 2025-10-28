package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Oss;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OssRepository extends JpaRepository<Oss,Long> {

    Optional<Oss> findByIdentifier(String identifier);
}
