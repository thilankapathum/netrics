package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.DistrictCodeDto;
import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.district.DistrictCode;
import dev.thilanka.netrics.entity.KpiDay;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.DistrictCodeRepository;
import dev.thilanka.netrics.service.DistrictCodeService;
import dev.thilanka.netrics.service.DistrictService;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictCodeServiceImpl implements DistrictCodeService {
    private final DistrictCodeRepository districtCodeRepository;
    private final DistrictService districtService;
    private final KpiDayService kpiDayService;
    private final RatService ratService;
    private final Mapper mapper;

    @Override
    public List<DistrictCodeDto> getAll() {
        List<DistrictCode> codes = districtCodeRepository.findAll();
        return codes.stream().map(mapper::districtCodeToDto).toList();
    }

    @Override
    public DistrictCodeDto createDistrictCode(DistrictCodeDto dto) {

        District district = districtService.findDistrictByName(dto.districtName());
        List<Rat> rats = ratService.findAll();

        DistrictCode districtCode = mapper.toDistrictCode(dto);
        districtCode.setDistrict(district);

        DistrictCode savedDistrictCode = districtCodeRepository.save(districtCode);

        for (Rat rat : rats) {
            updateKpiDayWithoutDistrict(rat.getName());
        }

        return mapper.districtCodeToDto(savedDistrictCode);

    }

    @Override
    public List<DistrictCodeDto> createDistrictCodeList(List<DistrictCodeDto> dtos) {

        List<DistrictCodeDto> districtCodeDtos = new ArrayList<>();

        for (DistrictCodeDto dto : dtos) {
            districtCodeDtos.add(createDistrictCode(dto));
        }

        return districtCodeDtos;
    }

    private void updateKpiDayWithoutDistrict(String ratName) {
        List<KpiDay> kpiList = kpiDayService.getKpiWithoutDistrict(ratName);
        List<KpiDataDto> kpiDayDtos = new ArrayList<>();
        for (KpiDay kpi : kpiList) {
            kpi.setDistrictCode(getDistrictCodeByCellName(kpi.getCellName()));
            kpiDayDtos.add(kpiDayService.createLteFddKpiDay(kpi));
        }

        //todo: Check for cells which match with new district_code only
    }

    private DistrictCode getDistrictCodeByCellName(String cellName) {
        return districtCodeRepository.findDistrictCodeByPrefix(cellName)
                .orElse(null);
    }
}
