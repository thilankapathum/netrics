package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AlarmFieldMappingDto;

import java.util.List;

public interface AlarmFieldMappingService {

    List<AlarmFieldMappingDto> findByAlarmSource(String sourceName);
}
