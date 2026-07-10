package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.DistrictCodeDto;
import dev.thilanka.netrics.entity.district.DistrictCode;

import java.util.List;
import java.util.Optional;

public interface DistrictCodeService {

    void init();

    List<DistrictCodeDto> getAll();

    DistrictCodeDto createDistrictCode(DistrictCodeDto dto);

    List<DistrictCodeDto> createDistrictCodeList(List<DistrictCodeDto> dtos);

    DistrictCode findByDistrictCode(String districtCode);

    void updateKpiDayWithoutDistrict();

    void reload();

    Optional<DistrictCode> resolveByCellName(String cellName);
}
