package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.LteFddBasicKpiDto;
import jakarta.validation.Valid;

import java.util.List;

public interface LteFddBasicKpiService {
    List<LteFddBasicKpiDto> getAll();

    LteFddBasicKpiDto createBasicKpi(@Valid LteFddBasicKpiDto dto);
}
