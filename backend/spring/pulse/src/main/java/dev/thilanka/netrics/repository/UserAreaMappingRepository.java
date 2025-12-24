package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.UserAreaMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAreaMappingRepository extends JpaRepository<UserAreaMapping, Long> {

    Optional<UserAreaMapping> findByUserId(String userId);
}
