package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.entity.district.District;

import java.util.List;

public interface DistrictService {

    List<DistrictDto> getAll();

    DistrictDto createDistrict(DistrictDto dto);

    List<DistrictDto> createDistrictList(List<DistrictDto> dtos);

    District findDistrictByName(String name);
}
