package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.AreaDistrictCodeMappingDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.AreaDistrictCodeMapping;
import dev.thilanka.netrics.entity.district.DistrictCode;
import dev.thilanka.netrics.repository.AreaDistrictCodeMappingRepository;
import dev.thilanka.netrics.service.AreaDistrictCodeMappingService;
import dev.thilanka.netrics.service.AreaService;
import dev.thilanka.netrics.service.DistrictCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaDistrictCodeMappingServiceImpl implements AreaDistrictCodeMappingService {

    private final AreaDistrictCodeMappingRepository areaDistrictCodeMappingRepository;
    private final AreaService areaService;
    private final DistrictCodeService districtCodeService;

    @Override
    public AreaDistrictCodeMapping createAreaDistrictCodeMapping(AreaDistrictCodeMapping mapping) {
        return areaDistrictCodeMappingRepository.save(mapping);
    }

    @Override
    public AreaDistrictCodeMappingDto createAreaDistrictCodeMapping(AreaDistrictCodeMappingDto dto) {

        Area area = areaService.findAreaByName(dto.areaName());
        DistrictCode districtCode = districtCodeService.findByDistrictCode(dto.districtCode());

        AreaDistrictCodeMapping mapping = AreaDistrictCodeMapping
                .builder()
                .area(area)
                .districtCode(districtCode)
                .build();

        AreaDistrictCodeMapping savedAreaDistrictCodeMapping = createAreaDistrictCodeMapping(mapping);

        return toDto(savedAreaDistrictCodeMapping);
    }

    @Override
    public List<AreaDistrictCodeMappingDto> createAreaDistrictCodeMappings(List<AreaDistrictCodeMappingDto> dtos) {
        List<AreaDistrictCodeMappingDto> list = new ArrayList<>();

        for (AreaDistrictCodeMappingDto dto : dtos) {
            try {
                list.add(createAreaDistrictCodeMapping(dto));
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
        return list;
    }

    @Override
    public List<AreaDistrictCodeMappingDto> getAll() {
        return areaDistrictCodeMappingRepository
                .findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void deleteAreaDistrictCodeMapping(Long id) {
        if (!areaDistrictCodeMappingRepository.existsById(id)) {
            throw new ResourceNotFoundException("AreaDistrictCodeMapping", "ID", id);
        }
        areaDistrictCodeMappingRepository.deleteById(id);
    }

    private AreaDistrictCodeMappingDto toDto(AreaDistrictCodeMapping mapping) {
        return new AreaDistrictCodeMappingDto(
                mapping.getId(),
                mapping.getArea().getName(),
                mapping.getDistrictCode().getCode());
    }
}
