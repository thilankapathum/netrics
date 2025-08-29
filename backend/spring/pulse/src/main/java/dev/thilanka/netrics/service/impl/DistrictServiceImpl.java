package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.DistrictRepository;
import dev.thilanka.netrics.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {
    private final DistrictRepository districtRepository;
    private final Mapper mapper;

    @Override
    public List<DistrictDto> getAll() {
        List<District> districts = districtRepository.findAll();
        return districts.stream().map(mapper::districtToDto).toList();
    }

    @Override
    public DistrictDto createDistrict(DistrictDto dto) {
        District district = mapper.toDistrict(dto);
        District savedDistrict = districtRepository.save(district);
        return mapper.districtToDto(savedDistrict);
    }

    @Override
    public List<DistrictDto> createDistrictList(List<DistrictDto> dtos) {
        List<DistrictDto> districtDtos = new ArrayList<>();

        for (DistrictDto districtDto : dtos) {
            districtDtos.add(createDistrict(districtDto));
        }
        return districtDtos;
    }

    @Override
    public District findDistrictByName(String name) {
        return districtRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Cannot find District by: " + name));
    }
}
