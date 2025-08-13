package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpiData;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpiSnap;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDaySnap;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();

    List<KpiDataFractionDto> findAllWithFractions();

    List<LteFddKpiDaySnap> getAverage();

    LteFddKpiDaySnap[] getKpiSnapshot(String standardKpiName, String period);

    LteFddBasicKpiData getBasicKpiSnapshot(String basicKpiName, String period);

    List<LteFddBasicKpiSnap> getBasicSnapshot(String basicKpiName, String period);
}
