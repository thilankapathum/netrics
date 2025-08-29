package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.DistrictCodeDto;
import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.district.DistrictCode;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.DistrictCodeRepository;
import dev.thilanka.netrics.service.DistrictCodeService;
import dev.thilanka.netrics.service.DistrictService;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictCodeServiceImpl implements DistrictCodeService {
    private final DistrictCodeRepository districtCodeRepository;
    private final DistrictService districtService;
    private final LteFddKpiDayService lteFddKpiDayService;
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
        updateLteFddKpiDayWithoutDistrict();    //todo: Update for other RATs

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

    private List<KpiDataDto> updateLteFddKpiDayWithoutDistrict() {
        List<LteFddKpiDay> kpiList = lteFddKpiDayService.getKpiWithoutDistrict();
        List<KpiDataDto> kpiDayDtos = new ArrayList<>();
        for (LteFddKpiDay kpi : kpiList) {
            kpi.setDistrictCode(getDistrictCodeByCellName(kpi.getCellName()));
            kpiDayDtos.add(lteFddKpiDayService.createLteFddKpiDay(kpi));
        }
        return kpiDayDtos;

        //todo: Check for cells which match with new district_code only
    }

    private DistrictCode getDistrictCodeByCellName(String cellName) {
        return districtCodeRepository.findDistrictCodeByPrefix(cellName)
                .orElse(null);
    }
}
