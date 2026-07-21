package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.alarms.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmTypeRepository extends JpaRepository<AlarmType, Long> {
}
