package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AlarmSourceDto;
import dev.thilanka.netrics.entity.alarms.AlarmSource;

import java.util.List;

public interface AlarmSourceService {

    AlarmSource createAlarmSource(AlarmSource alarmSource);

    AlarmSourceDto createAlarmSource(AlarmSourceDto dto);

    AlarmSource findAlarmSource(String name);

    AlarmSourceDto getAlarmSource(String name);

    List<AlarmSource> findAll();

    List<AlarmSourceDto> getAll();

    List<AlarmSource> createAlarmSources(List<AlarmSource> alarmSources);

    List<AlarmSourceDto> createAlarmSourceDtos(List<AlarmSourceDto> dtos);
}
