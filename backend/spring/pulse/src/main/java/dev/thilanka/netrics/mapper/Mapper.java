package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.district.DistrictCode;
import dev.thilanka.netrics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Mapper {
    private final RatRepository ratRepository;
    private final StandardKpiRepository standardKpiRepository;
    private final AreaRepository areaRepository;
    private final WorstCellRepository worstCellRepository;
    private final GranularityRepository granularityRepository;

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


// ----- StandardKpi -----

    public StandardKpiDto standardKpiToDto(StandardKpi standardKpi) {

        String basicKpi = "";
        if (standardKpi.getBasicKpi() != null)
            basicKpi = standardKpi.getBasicKpi().getKpiName();

        return new StandardKpiDto(
                standardKpi.getKpiName(),
                standardKpi.getLabel(),
                standardKpi.getUnit(),
                standardKpi.getType(),
                standardKpi.getWorstOrder(),
                standardKpi.getThreshold(),
                standardKpi.getAggregation(),
                basicKpi,
                standardKpi.getRat().getName());
    }

    public StandardKpi toStandardKpi(StandardKpiDto standardKpiDto) {

        Rat rat = ratRepository.findByName(standardKpiDto.ratName())
                .orElseThrow(() -> new RuntimeException("RAT not found by name: " + standardKpiDto.ratName()));

        return StandardKpi
                .builder()
                .kpiName(standardKpiDto.kpiName())
                .label(standardKpiDto.label())
                .unit(standardKpiDto.unit())
                .type(standardKpiDto.type())
                .aggregation(standardKpiDto.aggregation())
                .worstOrder(standardKpiDto.worstOrder())
                .threshold(standardKpiDto.threshold())
                .rat(rat)
                .build();

        //-- Basic KPI should be queried and set in the Service because otherwise Mapper will throw Circular Dependency (if Service is injected to Mapper)
    }

// ----- KpiMappingToOss -----

    public KpiMappingToOssDto kpiMappingToDto(KpiMappingToOss mapping) {

        return new KpiMappingToOssDto(
                mapping.getOssKpiName(),
                mapping.getMultiplicationFactor(),
                mapping.getOss().getIdentifier(),
                mapping.getStandardKpi().getKpiName(),
                mapping.getRat().getName());
    }

// ----- CellKpiData -----

    public KpiDataDto kpiDayToKpiDataDto(KpiDay kpiDay) {

        return new KpiDataDto(
                Timestamp.valueOf(kpiDay.getTimestamp()),
                kpiDay.getCellName(),
                kpiDay.getStandardKpi().getKpiName(),
                kpiDay.getKpiValue()
        );
    }

    // ----- StandardRawKpiMapping -----

    public StandardRawKpiMappingDto standardRawKpiMappingToDto(StandardRawKpiMapping mapping) {

        return new StandardRawKpiMappingDto(
                mapping.getStandardKpi().getKpiName(),
                mapping.getNumerator().getKpiName(),
                mapping.getDenominator().getKpiName(),
                mapping.getRat().getName()
        );
    }

// ----- BasicKpi -----

    public BasicKpi toBasicKpi(BasicKpiDto dto) {
        Rat rat = ratRepository.findByName(dto.ratName())
                .orElseThrow(() -> new RuntimeException("RAT not found by name: " + dto.ratName()));

        return BasicKpi.builder()
                .kpiName(dto.kpiName())
                .label(dto.label())
                .worstOrder(dto.worstOrder())
                .threshold(dto.threshold())
                .aggregation(dto.aggregation())
                .unit(dto.unit())
                .rat(rat)
                .build();
    }

    public BasicKpiDto basicKpiToDto(BasicKpi kpi) {
        return new BasicKpiDto(kpi.getKpiName(), kpi.getLabel(), kpi.getWorstOrder(), kpi.getThreshold(), kpi.getAggregation(), kpi.getUnit(), kpi.getRat().getName());
    }

    public BasicKpiWithStandardKpiDto basicKpiToBasicKpiWithStandardKpiDto(BasicKpi basicKpi) {

        List<StandardKpiDto> standardKpiDtos = new ArrayList<>();

        for (StandardKpi kpi : basicKpi.getStandardKpis()) {
            standardKpiDtos.add(standardKpiToDto(kpi));
        }

        return new BasicKpiWithStandardKpiDto(
                basicKpi.getKpiName(),
                basicKpi.getLabel(),
                basicKpi.getWorstOrder(),
                basicKpi.getThreshold(),
                basicKpi.getAggregation(),
                basicKpi.getUnit(),
                basicKpi.getRat().getName(),
                standardKpiDtos);

    }

//-------- KpiData KpiDataDto -----------------------------------

    public KpiDataDto kpiDataToDto(KpiData kpiData) {
        return new KpiDataDto(kpiData.getTimestamp(), kpiData.getCellName(), kpiData.getKpiLabel(), kpiData.getKpiValue());
    }

//-------- KpiTrend KpiTrendDto -----------------------------------

    public KpiTrendDto kpiTrendToDto(KpiTrend kpiTrend) {
        return new KpiTrendDto(kpiTrend.getTimestamp(), kpiTrend.getKpiLabel(), kpiTrend.getKpiValue());
    }

    //------- DISTRICT DISTRICT-DTO -------------------------------------

    public DistrictDto districtToDto(District district) {
        return new DistrictDto(district.getName(), district.getCode());
    }

    public District toDistrict(DistrictDto dto) {
        return District.builder()
                .code(dto.code())
                .name(dto.name())
                .build();
    }
    //------------- DISTRICT-CODE DTO --------------------------------------

    public DistrictCodeDto districtCodeToDto(DistrictCode code) {
        return new DistrictCodeDto(code.getCode(), code.getCategory(), code.getDistrict().getName());
    }

    public DistrictCode toDistrictCode(DistrictCodeDto codeDto) {
        return DistrictCode.builder()
                .code(codeDto.code())
                .category(codeDto.category())
                .build();       //-- District is not included.
    }

    //-----------------LTEFDDKPIDAY KPIDAYDTO ----------------------------------

//    public KpiDayDto LteFddKpiDayToKpiDayDto(KpiDay kpiDay){
//        return new KpiDayDto(kpiDay.getTimestamp(),
//                kpiDay.getCellName(),
//                kpiDay.getSiteName(), kpiDay.getKpiValue(), kpiDay.getNumeratorKpiValue(), kpiDay.getDenominatorKpiValue(),
//                kpiDay.getDataType(), kpiDay.getFileName(), kpiDay.getStandardKpi().getId(),
//                kpiDay.getNumeratorKpi().getId(),
//                kpiDay.getDenominatorKpi().getId(),
//                kpiDay.getOss().getId(),kpiDay.getDistrictCode().getId());
//    }

    //-----------------KpiSnapshotDto KpiSnapshot ----------------------------------------

    public KpiSnapshot toKpiSnapshot(KpiSnapshotDto dto) {
        return new KpiSnapshot(dto.kpiLabel(), dto.unit(), dto.value(), dto.previousValue(), dto.difference(),
                dto.improved() > 0);
    }

    //================ RAT =====================

    public RatDto toRatDto(Rat rat) {
        return new RatDto(rat.getName(), rat.getLabel());
    }

    public Rat ratDtoToRat(RatDto dto) {
        return Rat.builder()
                .name(dto.name())
                .label(dto.label())
                .build();
    }

//    =============== WORST-CELL DASHBOARD =====================

    public WorstCell worstCellSaveDtoToWorstCell(WorstCellSaveDto dto, String period, String areaName, LocalDateTime timestamp) {

        Rat rat = ratRepository.findById(dto.ratId()).orElseThrow(
                () -> new RuntimeException("RAT not found by ID: " + dto.ratId())
        );

        Granularity granularity = granularityRepository.findById(dto.granularityId())
                .orElseThrow(() -> new RuntimeException("Granularity not found by: " + dto.granularityId()));

        StandardKpi standardKpi = standardKpiRepository.findById(dto.standardKpiId())
                .orElseThrow(() -> new RuntimeException("Standard KPI not found by: " + dto.standardKpiId()));

        Area area = areaRepository.findByName(areaName)
                .orElseThrow(() -> new RuntimeException("Area not found by: " + areaName));

        return WorstCell.builder()
                .timestamp(timestamp)
                .cellName(dto.cellName())
//                .unit(dto.unit())
                .value(dto.value())
                .previousValue(dto.previousValue())
                .difference(dto.difference())
                .improved(dto.improved() > 0)
                .period(period)
                .area(area)
                .rat(rat)
                .standardKpi(standardKpi)
                .excludeZeroes(dto.excludeZeroes())
                .granularity(granularity)
                .build();
    }

    public WorstCellSaveDto toWorstCellSaveDto(WorstCell worstCell) {

        int isImproved = 0;
        if (worstCell.isImproved()) isImproved = 1;

        return new WorstCellSaveDto(
                Timestamp.valueOf(worstCell.getTimestamp()),
                worstCell.getCellName(),
                worstCell.getStandardKpi().getId(),
                worstCell.getStandardKpi().getUnit(),
                worstCell.getValue(),
                worstCell.getPreviousValue(),
                worstCell.getDifference(),
                isImproved,
                worstCell.getRat().getId(),
                worstCell.isExcludeZeroes(),
                worstCell.getGranularity().getId()
        );
    }

//    =========== USER =======================

    public UserDto userToDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail()
        );
    }

//    ================ WORST CELL COMMENT ===============

    public WorstCellCommentDto worstCellCommentToDto(WorstCellComment worstCellComment) {
        return new WorstCellCommentDto(worstCellComment.getId(),
                worstCellComment.getComment(),
                worstCellComment.getWorstCell().getId(),
                worstCellComment.getCreatedAt(),
                worstCellComment.getLastModifiedAt(),
                worstCellComment.getCreatedBy(),
                worstCellComment.getLastModifiedBy());
    }

    public WorstCellComment dtoToWorstCellComment(WorstCellCommentDto dto) {
        WorstCell worstCell = worstCellRepository.findById(dto.worstCellId())
                .orElseThrow(() -> new RuntimeException("Worst cell not found by ID: " + dto.worstCellId()));

        return WorstCellComment.builder()
                .comment(dto.comment())
                .worstCell(worstCell)
                .build();
    }

    // ================ CELL ==========================

    public CellDto cellToDto(Cell cell) {
        return new CellDto(
                cell.getCellName(),
                cell.getNodeName(),
                cell.getRat() != null ? cell.getRat().getName() : null,
                cell.getSite() != null ? cell.getSite().getSiteCode() : null,
                cell.getBand() != null ? cell.getBand().getName() : null,
                cell.getAzimuth(),
                cell.getBeamwidth(),
                cell.isMultiBeam(),
                cell.getCarrier() != null ? cell.getCarrier().getName() : null,
                cell.getSector() != null ? cell.getSector().getName() : null
        );
    }

    // ================= CARRIER ======================

    public CarrierDto carrierToDto(Carrier carrier) {
        return new CarrierDto(carrier.getName(), carrier.getRadius(), carrier.getRat().getName(), carrier.getBand().getName());
    }

    // ================= SITES ========================

    public SiteDto siteToDto(Site site) {
        return new SiteDto(site.getSiteCode(), site.getSiteName(), site.getLatitude(), site.getLongitude());
    }

    // ================ SECTOR ========================

    public SectorDto sectorToDto(Sector sector) {
        return new SectorDto(sector.getSectorIndex(), sector.getName(), sector.getAzimuth(), sector.getSite().getSiteCode());
    }

    //=================     MAP-CELL-THRESHOLD-SET ======================
    public MapCellThrSetDto mapCellThrSetToDto(MapCellThrSet thrSet) {
        return new MapCellThrSetDto(
                thrSet.getStandardKpi().getKpiName(),
                thrSet.getGranularity().getName(),
                thrSet.getRat().getName(),
                thrSet.getUserId(),
                thrSet.isAdmin(),
                thrSet.isActive(),
                thrSet.isDeleted(),
                thrSet.getCreatedAt(),
                thrSet.getLastModifiedAt(),
                thrSet.getCreatedBy(),
                thrSet.getLastModifiedBy()
        );
    }
}
