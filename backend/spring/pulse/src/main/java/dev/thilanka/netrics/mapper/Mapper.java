package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.KpiData;
import dev.thilanka.netrics.entity.KpiTrend;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.district.DistrictCode;
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

        String basicKpi = "";
        if (lteFddStandardKpi.getLteFddBasicKpi() != null)
            basicKpi = lteFddStandardKpi.getLteFddBasicKpi().getKpiName();

        return new StandardKpiDto(
                lteFddStandardKpi.getKpiName(),
                lteFddStandardKpi.getLabel(),
                lteFddStandardKpi.getUnit(),
                lteFddStandardKpi.getType(),
                lteFddStandardKpi.getWorstOrder(),
                lteFddStandardKpi.getThreshold(),
                lteFddStandardKpi.getAggregation(),
                basicKpi);
    }

    public LteFddStandardKpi toLteFddStandardKpi(StandardKpiDto standardKpiDto) {

        return LteFddStandardKpi
                .builder()
                .kpiName(standardKpiDto.kpiName())
                .label(standardKpiDto.label())
                .unit(standardKpiDto.unit())
                .type(standardKpiDto.type())
                .aggregation(standardKpiDto.aggregation())
                .worstOrder(standardKpiDto.worstOrder())
                .threshold(standardKpiDto.threshold())
                .build();

        //-- Basic KPI should be queried and set in the Service because otherwise Mapper will throw Circular Dependency (if Service is injected to Mapper)
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
                .unit(dto.unit())
                .build();
    }

    public BasicKpiDto lteFddBasicKpiToDto(LteFddBasicKpi kpi) {
        return new BasicKpiDto(kpi.getKpiName(), kpi.getLabel(), kpi.getWorstOrder(), kpi.getThreshold(), kpi.getAggregation(), kpi.getUnit());
    }

//-------- KpiData KpiDataDto -----------------------------------

    public KpiDataDto kpiDataToDto(KpiData kpiData) {
        return new KpiDataDto(kpiData.getTimestamp().toLocalDateTime(), kpiData.getCellName(), kpiData.getKpiLabel(), kpiData.getKpiValue());
    }

//-------- KpiTrend KpiTrendDto -----------------------------------

    public KpiTrendDto kpiTrendToDto(KpiTrend kpiTrend){
        return new KpiTrendDto(kpiTrend.getTimestamp(), kpiTrend.getKpiLabel(), kpiTrend.getKpiValue());
    }

    //------- DISTRICT DISTRICT-DTO -------------------------------------

    public DistrictDto districtToDto(District district){
        return new DistrictDto(district.getName(), district.getCode());
    }

    public District toDistrict(DistrictDto dto){
        return District.builder()
                .code(dto.code())
                .name(dto.name())
                .build();
    }
    //------------- DISTRICT-CODE DTO --------------------------------------

    public DistrictCodeDto districtCodeToDto(DistrictCode code){
        return new DistrictCodeDto(code.getCode(), code.getCategory(), code.getDistrict().getName());
    }

    public DistrictCode toDistrictCode(DistrictCodeDto codeDto){
        return DistrictCode.builder()
                .code(codeDto.code())
                .category(codeDto.category())
                .build();       //-- District is not included.
    }

    //-----------------LTEFDDKPIDAY KPIDAYDTO ----------------------------------

//    public KpiDayDto LteFddKpiDayToKpiDayDto(LteFddKpiDay kpiDay){
//        return new KpiDayDto(kpiDay.getTimestamp(),
//                kpiDay.getCellName(),
//                kpiDay.getSiteName(), kpiDay.getKpiValue(), kpiDay.getNumeratorKpiValue(), kpiDay.getDenominatorKpiValue(),
//                kpiDay.getDataType(), kpiDay.getFileName(), kpiDay.getLteFddStandardKpi().getId(),
//                kpiDay.getNumeratorKpi().getId(),
//                kpiDay.getDenominatorKpi().getId(),
//                kpiDay.getOss().getId(),kpiDay.getDistrictCode().getId());
//    }

}
