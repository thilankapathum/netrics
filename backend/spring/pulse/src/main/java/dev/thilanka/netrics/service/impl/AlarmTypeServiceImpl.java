package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.AlarmTypeDto;
import dev.thilanka.netrics.entity.alarms.AlarmType;
import dev.thilanka.netrics.repository.AlarmTypeRepository;
import dev.thilanka.netrics.service.AlarmTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlarmTypeServiceImpl implements AlarmTypeService {
    private final AlarmTypeRepository alarmTypeRepository;


    @Override
    public List<AlarmType> findAll() {
        return alarmTypeRepository.findAll();
    }

    @Override
    public List<AlarmTypeDto> getAll() {
        return findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private AlarmTypeDto toDto(AlarmType alarmType) {
        return new AlarmTypeDto(alarmType.getName());
    }
}
