package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistrictCodeServiceImpl implements DistrictCodeService {
    private final DistrictCodeRepository districtCodeRepository;
    private final DistrictService districtService;
    private final KpiDayService kpiDayService;
    private final RatService ratService;
    private final Mapper mapper;
    private volatile List<DistrictCode> sortedCodes = List.of();

    @PostConstruct
    public void init() {
        reload();
    }



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
        log.info("DistrictCode {} - {} saved successfully", savedDistrictCode.getCode(), savedDistrictCode.getDistrict().getName());

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


//          --- MANUALLY UPDATE KPI'S DISTRICT CODES ---
//        for (Rat rat : rats) {
//            updateKpiDayWithoutDistrict(rat.getName());
//        }

        return mapper.districtCodeToDto(savedDistrictCode);

    }

    @Override
    public List<DistrictCodeDto> createDistrictCodeList(List<DistrictCodeDto> dtos) {

        List<DistrictCodeDto> districtCodeDtos = new ArrayList<>();
        log.info("Creating District Codes");

        for (DistrictCodeDto dto : dtos) {
            log.info("Creating District Code {} - {}", dto.code(), dto.districtName());
            districtCodeDtos.add(createDistrictCode(dto));
            log.info("Created District Code {} - {}", dto.code(), dto.districtName());
        }

        return districtCodeDtos;
    }

    @Override
    public DistrictCode findByDistrictCode(String districtCode) {
        return districtCodeRepository.findByCode(districtCode)
                .orElseThrow(() -> new ResourceNotFoundException("District Code", "Code", districtCode));
    }

    @Override
    public void updateKpiDayWithoutDistrict() {
        List<Rat> rats = ratService.findAll();
        for (Rat rat : rats) {
            updateKpiDayWithoutDistrict(rat.getName());
        }
    }

    @Override
    public void reload() {
        List<DistrictCode> codes = districtCodeRepository.findAll();
        codes.sort(Comparator.comparingInt((DistrictCode d) -> d.getCode().length()).reversed());
        this.sortedCodes = codes;
        log.info("Loaded {} district codes", sortedCodes.size());
    }

    @Override
    public Optional<DistrictCode> resolveByCellName(String cellName) {

        if (sortedCodes.isEmpty()) {
            reload();
        }

        if (cellName == null || cellName.isBlank()) {
            return Optional.empty();
        }
        String trimmed = cellName.trim();
        return sortedCodes.stream()
                .filter(dc -> trimmed.startsWith(dc.getCode()))
                .findFirst();
    }

    private void updateKpiDayWithoutDistrict(String ratName) {
        List<KpiDay> kpiList = kpiDayService.getKpiWithoutDistrict(ratName);
        List<KpiDataDto> kpiDayDtos = new ArrayList<>();
        for (KpiDay kpi : kpiList) {
            kpi.setDistrictCode(getDistrictCodeByCellName(kpi.getCellName()));
            kpiDayDtos.add(kpiDayService.createLteFddKpiDay(kpi));
        }
        log.info("Updated KPI Day without District Code {}", ratName);
        //todo: Check for cells which match with new district_code only
    }

    private DistrictCode getDistrictCodeByCellName(String cellName) {
        return districtCodeRepository.findDistrictCodeByPrefix(cellName)
                .orElse(null);
    }
}
