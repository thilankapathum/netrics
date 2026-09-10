package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.alarms.AlarmSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmSourceRepository extends JpaRepository<AlarmSource,Long> {
    Optional<AlarmSource> findAlarmSourceByName(String name);
}
