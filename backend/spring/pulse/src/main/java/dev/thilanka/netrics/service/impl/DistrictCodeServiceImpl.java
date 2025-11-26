package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.DistrictCodeDto;
import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.AreaDistrictCodeMapping;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.district.DistrictCode;
import dev.thilanka.netrics.entity.KpiDay;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.DistrictCodeRepository;
import dev.thilanka.netrics.service.*;
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
//    private final AreaDistrictCodeMappingService areaDistrictCodeMappingService;
//    private final AreaService areaService;

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

        //-- Assign any newly created District Code for 'All Districts' area.
//        try {
//            Area area = areaService.findAreaByName("All Districts");
//            AreaDistrictCodeMapping areaDistrictCodeMapping = AreaDistrictCodeMapping
//                    .builder()
//                    .area(area)
//                    .districtCode(savedDistrictCode)
//                    .build();
//            areaDistrictCodeMappingService.createAreaDistrictCodeMapping(areaDistrictCodeMapping);
//            System.out.println("'All Districts' mapped");
//        } catch (Exception e) {
//            System.out.println("Error mapping 'All Districts' to " + savedDistrictCode.getCode());
//        }

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

    @Override
    public DistrictCode findByDistrictCode(String districtCode) {
        return districtCodeRepository.findByCode(districtCode)
                .orElseThrow(() -> new RuntimeException("District code not found by: " + districtCode));
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
