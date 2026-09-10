package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AlarmTypeDto;
import dev.thilanka.netrics.entity.alarms.AlarmType;

import java.util.List;

public interface AlarmTypeService {
    List<AlarmType> findAll();
    List<AlarmTypeDto> getAll();
}
