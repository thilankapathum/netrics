package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.alarms.AlarmFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmFieldMappingRepository extends JpaRepository<AlarmFieldMapping, Long> {
    List<AlarmFieldMapping> findByAlarmSource_Name(String sourceName);

}
