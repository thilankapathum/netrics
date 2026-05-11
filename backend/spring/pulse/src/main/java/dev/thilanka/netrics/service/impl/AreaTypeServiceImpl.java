package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.AreaTypeDto;
import dev.thilanka.netrics.entity.AreaType;
import dev.thilanka.netrics.repository.AreaTypeRepository;
import dev.thilanka.netrics.service.AreaTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaTypeServiceImpl implements AreaTypeService {
    private final AreaTypeRepository areaTypeRepository;

    @Override
    public AreaType createAreaType(AreaType areaType) {
        return areaTypeRepository.save(areaType);
    }

    @Override
    public AreaTypeDto createAreaType(AreaTypeDto dto) {
        AreaType areaType = AreaType.builder().name(dto.name()).build();
        AreaType savedAreaType = createAreaType(areaType);
        return new AreaTypeDto(savedAreaType.getName());
    }

    @Override
    public List<AreaTypeDto> createAreaTypes(List<AreaTypeDto> dtos) {
        List<AreaTypeDto> areaTypeDtos = new ArrayList<>();
        for (AreaTypeDto dto: dtos){
            areaTypeDtos.add(createAreaType(dto));
        }
        return areaTypeDtos;
    }

    @Override
    public AreaType findAreaTypeByName(String name) {
        return areaTypeRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("AreaType", "Name", name));
    }

    @Override
    public List<AreaTypeDto> getAll() {
        List<AreaType> areaTypes = areaTypeRepository.findAll();

        return areaTypes.stream().map(at -> new AreaTypeDto(at.getName())).toList();
    }
}
