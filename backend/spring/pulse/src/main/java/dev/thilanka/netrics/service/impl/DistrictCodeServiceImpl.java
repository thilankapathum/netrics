package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.DistrictCodeDto;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.district.DistrictCode;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.DistrictCodeRepository;
import dev.thilanka.netrics.service.DistrictCodeService;
import dev.thilanka.netrics.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictCodeServiceImpl implements DistrictCodeService {
    private final DistrictCodeRepository districtCodeRepository;
    private final DistrictService districtService;
    private final Mapper mapper;

    @Override
    public List<DistrictCodeDto> getAll() {
        List<DistrictCode> codes = districtCodeRepository.findAll();
        return codes.stream().map(mapper::districtCodeToDto).toList();
    }

    @Override
    public DistrictCodeDto createDistrictCode(DistrictCodeDto dto) {

        District district = districtService.findDistrictByName(dto.districtName());

        DistrictCode districtCode = mapper.toDistrictCode(dto);
        districtCode.setDistrict(district);

        DistrictCode savedDistrictCode = districtCodeRepository.save(districtCode);

        return mapper.districtCodeToDto(savedDistrictCode);

    }

    @Override
    public List<DistrictCodeDto> createDistrictCodeList(List<DistrictCodeDto> dtos) {

        List<DistrictCodeDto> districtCodeDtos = new ArrayList<>();

        for (DistrictCodeDto dto : dtos){
            districtCodeDtos.add(createDistrictCode(dto));
        }

        return districtCodeDtos;
    }
}
