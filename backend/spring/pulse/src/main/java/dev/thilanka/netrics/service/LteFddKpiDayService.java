package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;

import java.util.List;

public interface LteFddKpiDayService {
    List<KpiDataDto> findAll();
}
