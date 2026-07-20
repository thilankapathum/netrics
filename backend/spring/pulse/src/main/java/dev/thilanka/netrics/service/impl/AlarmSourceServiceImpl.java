package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.AlarmSourceDto;
import dev.thilanka.netrics.entity.alarms.AlarmSource;
import dev.thilanka.netrics.repository.AlarmSourceRepository;
import dev.thilanka.netrics.service.AlarmSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AlarmSourceServiceImpl implements AlarmSourceService {
    private final AlarmSourceRepository alarmSourceRepository;


    @Override
    public AlarmSource createAlarmSource(AlarmSource alarmSource) {
        return alarmSourceRepository.save(alarmSource);
    }

    @Override
    public AlarmSourceDto createAlarmSource(AlarmSourceDto dto) {
        AlarmSource alarmSource = toAlarmSource(dto);
        return toAlarmSourceDto(createAlarmSource(alarmSource));
    }

    @Override
    public AlarmSource findAlarmSource(String name) {
        return alarmSourceRepository.findAlarmSourceByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("AlarmSource", "name", name));
    }

    @Override
    public AlarmSourceDto getAlarmSource(String name) {
        return toAlarmSourceDto(findAlarmSource(name));
    }

    @Override
    public List<AlarmSource> findAll() {
        return alarmSourceRepository.findAll();
    }

    @Override
    public List<AlarmSourceDto> getAll() {
        return findAll().stream().map(this::toAlarmSourceDto).collect(Collectors.toList());
    }

    @Override
    public List<AlarmSource> createAlarmSources(List<AlarmSource> alarmSources) {

        List<AlarmSource> sources = new ArrayList<>();
        int saved = 0;

        for (AlarmSource alarmSource : alarmSources) {
            try {
                sources.add(createAlarmSource(alarmSource));
                saved++;
            } catch (Exception e) {
                log.warn("Failed to save alarm source {} - {}", alarmSource.getName(), alarmSource.getLabel());
            }
        }
        log.info("Created {} alarm sources. Failed saving {} alarm sources", saved, alarmSources.size() - saved);

        return sources;
    }

    @Override
    public List<AlarmSourceDto> createAlarmSourceDtos(List<AlarmSourceDto> dtos) {
        List<AlarmSource> sources = dtos.stream().map(this::toAlarmSource).toList();
        return createAlarmSources(sources).stream().map(this::toAlarmSourceDto).collect(Collectors.toList());
    }

    private AlarmSource toAlarmSource(AlarmSourceDto dto) {
        return AlarmSource.builder()
                .name(dto.name())
                .label(dto.label())
                .build();
    }

    private AlarmSourceDto toAlarmSourceDto(AlarmSource alarmSource) {
        return new AlarmSourceDto(alarmSource.getName(), alarmSource.getLabel());
    }
}
