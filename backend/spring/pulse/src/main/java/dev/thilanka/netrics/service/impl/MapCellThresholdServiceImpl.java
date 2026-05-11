package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.MapCellThrSetAndThresholds;
import dev.thilanka.netrics.dto.MapCellThrSetResponseDto;
import dev.thilanka.netrics.dto.MapCellThresholdDto;
import dev.thilanka.netrics.entity.MapCellThrSet;
import dev.thilanka.netrics.entity.MapCellThreshold;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.MapCellThresholdRepository;
import dev.thilanka.netrics.service.MapCellThrSetService;
import dev.thilanka.netrics.service.MapCellThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
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
        MapCellThrSet thrSet = mapCellThrSetService.findThrSetById(id);
        List<MapCellThreshold> thresholds = mapCellThresholdRepository.findByMapCellThrSetId(id);
        List<MapCellThresholdDto> thresholdDtos = thresholds.stream().map(mapper::mapCellThresholdToDto).collect(Collectors.toList());
        return new MapCellThrSetAndThresholds(mapper.mapCellThrSetToResponseDto(thrSet), thresholdDtos);
    }

    @Override
    public MapCellThrSetAndThresholds getThrSetAndThresholds(String standardKpiName, String ratName, String granularityName, boolean isAdmin) {
        MapCellThrSetResponseDto thrSetResponse = mapCellThrSetService.getThrSetResponse(standardKpiName, ratName, granularityName, isAdmin);
        List<MapCellThreshold> thresholds = mapCellThresholdRepository.findByMapCellThrSetId(thrSetResponse.id());
        List<MapCellThresholdDto> thresholdDtos = thresholds.stream().map(mapper::mapCellThresholdToDto).collect(Collectors.toList());
        return new MapCellThrSetAndThresholds(thrSetResponse, thresholdDtos);
    }

    @Override
    public MapCellThrSetAndThresholds createMapCellThrSetAndThresholds(MapCellThrSetAndThresholds thrSetAndThresholds) {

        MapCellThrSet savedThrSet = mapCellThrSetService.createThrSet(thrSetAndThresholds.thrSet());
        List<MapCellThreshold> savedThresholds = new ArrayList<>();

        for (MapCellThresholdDto dto : thrSetAndThresholds.thresholds()) {
            MapCellThreshold threshold = MapCellThreshold.builder()
                    .minValue(dto.minValue())
                    .maxValue(dto.maxValue())
                    .color(dto.color())
                    .label(dto.label())
                    .priority(dto.priority())
                    .mapCellThrSet(savedThrSet)
                    .build();
            savedThresholds.add(mapCellThresholdRepository.save(threshold));
        }

        List<MapCellThresholdDto> savedThresholdDtos = savedThresholds.stream().map(mapper::mapCellThresholdToDto).collect(Collectors.toList());

        return new MapCellThrSetAndThresholds(mapper.mapCellThrSetToResponseDto(savedThrSet), savedThresholdDtos);
    }

    @Override
    public MapCellThrSetAndThresholds updateMapCellThrSetAndThresholds(MapCellThrSetAndThresholds thrSetAndThresholds) {

        MapCellThrSet thrSet = mapCellThrSetService.findThrSetById(thrSetAndThresholds.thrSet().id());
        List<MapCellThreshold> thresholds = mapCellThresholdRepository.findByMapCellThrSetId(thrSet.getId());
        Set<Integer> priorities = new HashSet<>();

        Map<Integer, MapCellThreshold> existingMap = thresholds.stream().collect(Collectors.toMap(MapCellThreshold::getPriority, t -> t));

        for (MapCellThresholdDto dto : thrSetAndThresholds.thresholds()) {

            if (dto.priority() != null && existingMap.containsKey(dto.priority())) {
                //update exiting
                MapCellThreshold threshold = existingMap.get(dto.priority());

                threshold.setMinValue(dto.minValue());
                threshold.setMaxValue(dto.maxValue());
                threshold.setColor(dto.color());
                threshold.setLabel(dto.label());

                MapCellThreshold updatedThreshold = mapCellThresholdRepository.save(threshold);

                priorities.add(dto.priority());
            } else {
                MapCellThresholdDto createdThreshold = createThreshold(dto);
                priorities.add(createdThreshold.priority());
            }
        }

        List<MapCellThreshold> updatedThresholds = mapCellThresholdRepository.findByMapCellThrSetId(thrSet.getId());

        List<MapCellThreshold> toDelete = updatedThresholds.stream()
                .filter(t -> !priorities.contains(t.getPriority()))
                .toList();

        mapCellThresholdRepository.deleteAll(toDelete);

        List<MapCellThreshold> finalThresholds = mapCellThresholdRepository.findByMapCellThrSetId(thrSet.getId());

        return new MapCellThrSetAndThresholds(
                mapper.mapCellThrSetToResponseDto(thrSet),
                finalThresholds.stream()
                        .map(mapper::mapCellThresholdToDto).collect(Collectors.toList()));
    }
}
