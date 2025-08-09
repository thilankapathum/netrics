package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.KpiData;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiMapping;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardRawKpiMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Mapper {
    public OssDto ossToDto(Oss oss){
        OssDto ossDto = new OssDto(oss.getOssName(),oss.getIdentifier(),oss.getVendor());
        return ossDto;
    }

    public Oss ossDtoToOss(OssDto ossDto){
        Oss oss = Oss.builder()
                .ossName(ossDto.ossName())
                .vendor(ossDto.vendor())
                .identifier(ossDto.identifier())
                .build();
        return oss;
    }

    public LteFddStandardKpiDto lteFddStandardKpiToDto(LteFddStandardKpi lteFddStandardKpi){
        LteFddStandardKpiDto lteFddStandardKpiDto = new LteFddStandardKpiDto(
                lteFddStandardKpi.getKpiName(),
                lteFddStandardKpi.getUnit(),
                lteFddStandardKpi.getType(),
                lteFddStandardKpi.getWorstOrder(),
                lteFddStandardKpi.getThreshold());

        return lteFddStandardKpiDto;
    }

    public LteFddStandardKpi toLteFddStandardKpi(LteFddStandardKpiDto lteFddStandardKpiDto){
        LteFddStandardKpi lteFddStandardKpi = LteFddStandardKpi
                .builder()
                .kpiName(lteFddStandardKpiDto.kpiName())
                .unit(lteFddStandardKpiDto.unit())
                .type(lteFddStandardKpiDto.type())
                .worstOrder(lteFddStandardKpiDto.worstOrder())
                .threshold(lteFddStandardKpiDto.threshold())
                .build();

        return lteFddStandardKpi;
    }

    public LteFddKpiMappingDto LteFddKpiMappingToDto(LteFddKpiMapping mapping){
        LteFddKpiMappingDto dto = new LteFddKpiMappingDto(
                mapping.getOssKpiName(),
                mapping.getMultiplicationFactor(),
                mapping.getOss().getIdentifier(),
                mapping.getLteFddStandardKpi().getKpiName());

        return dto;
    }

    public KpiDataDto LteFddKpiDayToKpiDataDto(LteFddKpiDay kpiDay){
        KpiDataDto kpiDataDto = new KpiDataDto(
                kpiDay.getTimestamp(),
                kpiDay.getCellName(),
                kpiDay.getLteFddStandardKpi().getKpiName(),
                kpiDay.getKpiValue()
        );

        return kpiDataDto;
    }

    // ----- LteFddStandardRawKpiMapping -----

    public LteFddStandardRawKpiMappingDto lteFddStandardRawKpiMappingToDto(LteFddStandardRawKpiMapping mapping){
        LteFddStandardRawKpiMappingDto dto = new LteFddStandardRawKpiMappingDto(
                mapping.getStandardKpi().getKpiName(),
                mapping.getNumerator().getKpiName(),
                mapping.getDenominator().getKpiName()
        );

        return dto;
    }

//    public LteFddStandardRawKpiMapping toLteFddStandardRawKpiMapping(LteFddStandardRawKpiMappingDto dto){
//
//        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(dto.standardKpi());
//        LteFddStandardKpi numerator = lteFddStandardKpiService.findByKpiName(dto.numerator());
//        LteFddStandardKpi denominator = lteFddStandardKpiService.findByKpiName(dto.denominator());
//
//
//        LteFddStandardRawKpiMapping mapping = LteFddStandardRawKpiMapping.builder()
//                .standardKpi(standardKpi)
//                .numerator(numerator)
//                .denominator(denominator)
//                .build();
//
//        return mapping;
//    }

}
