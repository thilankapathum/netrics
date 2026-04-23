package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.MapCellThrSetAndThresholds;
import dev.thilanka.netrics.dto.MapCellThresholdDto;
import dev.thilanka.netrics.entity.MapCellThrSet;
import dev.thilanka.netrics.entity.MapCellThreshold;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.MapCellThresholdRepository;
import dev.thilanka.netrics.service.MapCellThrSetService;
import dev.thilanka.netrics.service.MapCellThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapCellThresholdServiceImpl implements MapCellThresholdService {
    private final MapCellThresholdRepository mapCellThresholdRepository;
    private final MapCellThrSetService mapCellThrSetService;
    private final Mapper mapper;

    @Override
    public MapCellThreshold createThreshold(MapCellThreshold threshold) {
        return mapCellThresholdRepository.save(threshold);
    }

    @Override
    public MapCellThresholdDto createThreshold(MapCellThresholdDto dto) {

        MapCellThrSet thrSet = mapCellThrSetService.findThrSetById(dto.mapCellThrSetId());

        MapCellThreshold threshold = MapCellThreshold.builder()
                .minValue(dto.minValue())
                .maxValue(dto.maxValue())
                .color(dto.color())
                .label(dto.label())
                .priority(dto.priority())
                .mapCellThrSet(thrSet)
                .build();

        return mapper.mapCellThresholdToDto(createThreshold(threshold));
    }

    @Override
    public List<MapCellThreshold> findThresholdsByThrSetId(Long id) {
        return mapCellThresholdRepository.findByMapCellThrSetId(id);
    }

    @Override
    public List<MapCellThresholdDto> getThresholdsByThrSetId(Long id) {

        List<MapCellThreshold> thresholds = mapCellThresholdRepository.findByMapCellThrSetId(id);

        return thresholds.stream().map(mapper::mapCellThresholdToDto).collect(Collectors.toList());
    }

    @Override
    public MapCellThrSetAndThresholds getThrSetAndThresholdsByThrSetId(Long id) {
        MapCellThrSet thrSet =  mapCellThrSetService.findThrSetById(id);
        List<MapCellThreshold> thresholds = mapCellThresholdRepository.findByMapCellThrSetId(id);
        List<MapCellThresholdDto> thresholdDtos = thresholds.stream().map(mapper::mapCellThresholdToDto).collect(Collectors.toList());
        return new MapCellThrSetAndThresholds(mapper.mapCellThrSetToResponseDto(thrSet), thresholdDtos);
    }
}
