package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.ltefdd.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Mapper {

// ----- OSS -----

    public OssDto ossToDto(Oss oss) {
        return new OssDto(oss.getOssName(), oss.getIdentifier(), oss.getVendor());
    }

    public Oss ossDtoToOss(OssDto ossDto) {
        return Oss.builder()
                .ossName(ossDto.ossName())
                .vendor(ossDto.vendor())
                .identifier(ossDto.identifier())
                .build();
    }


// ----- LteFddStandardKpi -----

    public LteFddStandardKpiDto lteFddStandardKpiToDto(LteFddStandardKpi lteFddStandardKpi) {

        return new LteFddStandardKpiDto(
                lteFddStandardKpi.getKpiName(),
                lteFddStandardKpi.getLabel(),
                lteFddStandardKpi.getUnit(),
                lteFddStandardKpi.getType(),
                lteFddStandardKpi.getWorstOrder(),
                lteFddStandardKpi.getThreshold(),
                lteFddStandardKpi.getAggregation());
    }

    public LteFddStandardKpi toLteFddStandardKpi(LteFddStandardKpiDto lteFddStandardKpiDto) {

        return LteFddStandardKpi
                .builder()
                .kpiName(lteFddStandardKpiDto.kpiName())
                .label(lteFddStandardKpiDto.label())
                .unit(lteFddStandardKpiDto.unit())
                .type(lteFddStandardKpiDto.type())
                .worstOrder(lteFddStandardKpiDto.worstOrder())
                .threshold(lteFddStandardKpiDto.threshold())
                .build();
    }

// ----- LteFddKpiMappingToOss -----

    public LteFddKpiMappingToOssDto LteFddKpiMappingToDto(LteFddKpiMappingToOss mapping) {

        return new LteFddKpiMappingToOssDto(
                mapping.getOssKpiName(),
                mapping.getMultiplicationFactor(),
                mapping.getOss().getIdentifier(),
                mapping.getLteFddStandardKpi().getKpiName());
    }

// ----- WorstCellKpiData -----

    public KpiDataDto LteFddKpiDayToKpiDataDto(LteFddKpiDay kpiDay) {

        return new KpiDataDto(
                kpiDay.getTimestamp(),
                kpiDay.getCellName(),
                kpiDay.getLteFddStandardKpi().getKpiName(),
                kpiDay.getKpiValue()
        );
    }

    // ----- LteFddStandardRawKpiMapping -----

    public LteFddStandardRawKpiMappingDto lteFddStandardRawKpiMappingToDto(LteFddStandardRawKpiMapping mapping) {

        return new LteFddStandardRawKpiMappingDto(
                mapping.getStandardKpi().getKpiName(),
                mapping.getNumerator().getKpiName(),
                mapping.getDenominator().getKpiName()
        );
    }

// ----- LteFddBasicKpi -----

    public LteFddBasicKpi toLteFddBasicKpi(LteFddBasicKpiDto dto) {
        return LteFddBasicKpi.builder()
                .kpiName(dto.kpiName())
                .label(dto.label())
                .worstOrder(dto.worstOrder())
                .threshold(dto.threshold())
                .build();
    }

    public LteFddBasicKpiDto lteFddBasicKpiToDto(LteFddBasicKpi kpi) {
        return new LteFddBasicKpiDto(kpi.getKpiName(), kpi.getLabel(), kpi.getWorstOrder(), kpi.getThreshold());
    }
}
