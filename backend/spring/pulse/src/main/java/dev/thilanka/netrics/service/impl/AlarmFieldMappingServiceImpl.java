package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.AlarmFieldMappingDto;
import dev.thilanka.netrics.entity.alarms.AlarmFieldMapping;
import dev.thilanka.netrics.repository.AlarmFieldMappingRepository;
import dev.thilanka.netrics.service.AlarmFieldMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AlarmFieldMappingServiceImpl implements AlarmFieldMappingService {

    private final AlarmFieldMappingRepository alarmFieldMappingRepository;

    @Override
    public List<AlarmFieldMappingDto> findByAlarmSource(String sourceName) {
        List<AlarmFieldMapping> mappings = alarmFieldMappingRepository.findByAlarmSource_Name(sourceName);

        return mappings.stream().map(this::toDto).collect(Collectors.toList());
    }

    private AlarmFieldMappingDto toDto(AlarmFieldMapping mapping) {
        return new AlarmFieldMappingDto(
                mapping.getCanonicalField().name(),
                mapping.getSourceColumn(),
                mapping.getExtractionRegex(),
                mapping.getDefaultValue(),
                mapping.isRequired()
        );
    }
}
