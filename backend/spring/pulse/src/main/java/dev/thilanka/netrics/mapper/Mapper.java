package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.KpiData;
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

    public StandardKpiDto lteFddStandardKpiToDto(LteFddStandardKpi lteFddStandardKpi) {

        return new StandardKpiDto(
                lteFddStandardKpi.getKpiName(),
                lteFddStandardKpi.getLabel(),
                lteFddStandardKpi.getUnit(),
                lteFddStandardKpi.getType(),
                lteFddStandardKpi.getWorstOrder(),
                lteFddStandardKpi.getThreshold(),
                lteFddStandardKpi.getAggregation());
    }

    public LteFddStandardKpi toLteFddStandardKpi(StandardKpiDto standardKpiDto) {

        return LteFddStandardKpi
                .builder()
                .kpiName(standardKpiDto.kpiName())
                .label(standardKpiDto.label())
                .unit(standardKpiDto.unit())
                .type(standardKpiDto.type())
                .worstOrder(standardKpiDto.worstOrder())
                .threshold(standardKpiDto.threshold())
                .build();
    }

// ----- LteFddKpiMappingToOss -----

    public KpiMappingToOssDto LteFddKpiMappingToDto(LteFddKpiMappingToOss mapping) {

        return new KpiMappingToOssDto(
                mapping.getOssKpiName(),
                mapping.getMultiplicationFactor(),
                mapping.getOss().getIdentifier(),
                mapping.getLteFddStandardKpi().getKpiName());
    }

// ----- CellKpiData -----

    public KpiDataDto LteFddKpiDayToKpiDataDto(LteFddKpiDay kpiDay) {

        return new KpiDataDto(
                kpiDay.getTimestamp(),
                kpiDay.getCellName(),
                kpiDay.getLteFddStandardKpi().getKpiName(),
                kpiDay.getKpiValue()
        );
    }

    // ----- LteFddStandardRawKpiMapping -----

    public StandardRawKpiMappingDto lteFddStandardRawKpiMappingToDto(LteFddStandardRawKpiMapping mapping) {

        return new StandardRawKpiMappingDto(
                mapping.getStandardKpi().getKpiName(),
                mapping.getNumerator().getKpiName(),
                mapping.getDenominator().getKpiName()
        );
    }

// ----- LteFddBasicKpi -----

    public LteFddBasicKpi toLteFddBasicKpi(BasicKpiDto dto) {
        return LteFddBasicKpi.builder()
                .kpiName(dto.kpiName())
                .label(dto.label())
                .worstOrder(dto.worstOrder())
                .threshold(dto.threshold())
                .aggregation(dto.aggregation())
                .build();
    }

    public BasicKpiDto lteFddBasicKpiToDto(LteFddBasicKpi kpi) {
        return new BasicKpiDto(kpi.getKpiName(), kpi.getLabel(), kpi.getWorstOrder(), kpi.getThreshold(), kpi.getAggregation());
    }

//-------- KpiData KpiDataDto -----------------------------------

    public KpiDataDto kpiDataToDto(KpiData kpiData){
        return new KpiDataDto(kpiData.getTimestamp().toLocalDateTime(), kpiData.getCellName(), kpiData.getKpiLabel(), kpiData.getKpiValue());
    }

}
