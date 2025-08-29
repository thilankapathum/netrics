package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DistrictCodeDto;

import java.util.List;

public interface DistrictCodeService {

    List<DistrictCodeDto> getAll();

    DistrictCodeDto createDistrictCode(DistrictCodeDto dto);

    List<DistrictCodeDto> createDistrictCodeList(List<DistrictCodeDto> dtos);
}
