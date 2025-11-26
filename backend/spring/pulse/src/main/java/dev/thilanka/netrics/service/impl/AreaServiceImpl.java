package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.AreaType;
import dev.thilanka.netrics.repository.AreaRepository;
import dev.thilanka.netrics.service.AreaService;
import dev.thilanka.netrics.service.AreaTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {
    private final AreaRepository areaRepository;
    private final AreaTypeService areaTypeService;

    @Override
    public AreaDto createArea(AreaDto areaDto) {

        AreaType areaType = areaTypeService.findAreaTypeByName(areaDto.areaTypeName());

        Area area = Area.builder().name(areaDto.name())
                .enabled(areaDto.enabled())
                .areaType(areaType)
                .build();

        Area savedArea = areaRepository.save(area);
        return new AreaDto(savedArea.getName(), savedArea.isEnabled(), area.getAreaType().getName());
    }

    @Override
    public List<AreaDto> createAreas(List<AreaDto> dtos) {
        List<AreaDto> areaDtos = new ArrayList<>();

        for (AreaDto dto: dtos){
            areaDtos.add(createArea(dto));
        }
        return areaDtos;
    }

    @Override
    public AreaDto getAllAreas() {
        return null;
    }

    @Override
    public Area findAreaByName(String name) {
        return areaRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Area not found by: " + name));
    }

    @Override
    public AreaDto getAreaByName(String name) {
        Area area = findAreaByName(name);
        return new AreaDto(area.getName(), area.isEnabled(), area.getAreaType().getName());
    }

    @Override
    public List<AreaDto> getAreasByAreaType(String areaTypeName) {
        AreaType areaType = areaTypeService.findAreaTypeByName(areaTypeName);

        List<Area> areas = areaRepository.findByAreaType(areaType);

        return areas.stream().map(a -> new AreaDto(a.getName(),a.isEnabled(),a.getAreaType().getName())).toList();
    }
}
