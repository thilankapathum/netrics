package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.ltefdd.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Mapper {

// ----- OSS -----

    public OssDto ossToDto(Oss oss) {
        OssDto ossDto = new OssDto(oss.getOssName(), oss.getIdentifier(), oss.getVendor());
        return ossDto;
    }

    public Oss ossDtoToOss(OssDto ossDto) {
        Oss oss = Oss.builder()
                .ossName(ossDto.ossName())
                .vendor(ossDto.vendor())
                .identifier(ossDto.identifier())
                .build();
        return oss;
    }


// ----- LteFddSandardKpi -----

    public LteFddStandardKpiDto lteFddStandardKpiToDto(LteFddStandardKpi lteFddStandardKpi) {
        LteFddStandardKpiDto lteFddStandardKpiDto = new LteFddStandardKpiDto(
                lteFddStandardKpi.getKpiName(),
                lteFddStandardKpi.getLabel(),
                lteFddStandardKpi.getUnit(),
                lteFddStandardKpi.getType(),
                lteFddStandardKpi.getWorstOrder(),
                lteFddStandardKpi.getThreshold());

        return lteFddStandardKpiDto;
    }

    public LteFddStandardKpi toLteFddStandardKpi(LteFddStandardKpiDto lteFddStandardKpiDto) {
        LteFddStandardKpi lteFddStandardKpi = LteFddStandardKpi
                .builder()
                .kpiName(lteFddStandardKpiDto.kpiName())
                .label(lteFddStandardKpiDto.label())
                .unit(lteFddStandardKpiDto.unit())
                .type(lteFddStandardKpiDto.type())
                .worstOrder(lteFddStandardKpiDto.worstOrder())
                .threshold(lteFddStandardKpiDto.threshold())
                .build();

        return lteFddStandardKpi;
    }

// ----- LteFddKpiMapping -----

    public LteFddKpiMappingDto LteFddKpiMappingToDto(LteFddKpiMapping mapping) {
        LteFddKpiMappingDto dto = new LteFddKpiMappingDto(
                mapping.getOssKpiName(),
                mapping.getMultiplicationFactor(),
                mapping.getOss().getIdentifier(),
                mapping.getLteFddStandardKpi().getKpiName());

        return dto;
    }

// ----- KpiData -----

    public KpiDataDto LteFddKpiDayToKpiDataDto(LteFddKpiDay kpiDay) {
        KpiDataDto kpiDataDto = new KpiDataDto(
                kpiDay.getTimestamp(),
                kpiDay.getCellName(),
                kpiDay.getLteFddStandardKpi().getKpiName(),
                kpiDay.getKpiValue()
        );

        return kpiDataDto;
    }


    public KpiDataFractionDto lteFddKpiDayToKpiDataFractionDto(LteFddKpiDay kpiDay) {
        KpiDataFractionDto dto = new KpiDataFractionDto(
                kpiDay.getTimestamp(),
                kpiDay.getCellName(),
                kpiDay.getLteFddStandardKpi().getKpiName(),
                kpiDay.getKpiValue(),
                kpiDay.getNumeratorKpiValue(),
                kpiDay.getDenominatorKpiValue()
        );
        return dto;
    }

    // ----- LteFddStandardRawKpiMapping -----

    public LteFddStandardRawKpiMappingDto lteFddStandardRawKpiMappingToDto(LteFddStandardRawKpiMapping mapping) {
        LteFddStandardRawKpiMappingDto dto = new LteFddStandardRawKpiMappingDto(
                mapping.getStandardKpi().getKpiName(),
                mapping.getNumerator().getKpiName(),
                mapping.getDenominator().getKpiName()
        );

        return dto;
    }

// ----- LteFddBasicKpi -----

    public LteFddBasicKpi toLteFddBasicKpi(LteFddBasicKpiDto dto) {
        LteFddBasicKpi basicKpi = LteFddBasicKpi.builder()
                .kpiName(dto.kpiName())
                .label(dto.label())
                .build();
        return basicKpi;
    }

    public LteFddBasicKpiDto lteFddBasicKpiToDto(LteFddBasicKpi kpi) {
        LteFddBasicKpiDto dto = new LteFddBasicKpiDto(kpi.getKpiName(), kpi.getLabel());
        return dto;
    }


// ----- LteFddBasicKpiMapping -----

    public LteFddBasicKpiMappingDto lteFddBasicKpiMappingToDto(LteFddBasicKpiMapping mapping){
//        LteFddBasicKpiMappingDto dto = new LteFddBasicKpiMappingDto(mapping.getBasicKpi().getKpiName(),
//                mapping.getStandardKpi().getKpiName());

        return null;
    }

}
