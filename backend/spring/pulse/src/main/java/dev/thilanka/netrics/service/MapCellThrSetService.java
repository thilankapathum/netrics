package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.MapCellThrSetDto;
import dev.thilanka.netrics.dto.MapCellThrSetResponseDto;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.MapCellThrSet;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;

public interface MapCellThrSetService {

    MapCellThrSet createThrSet(MapCellThrSet thrSet);

    MapCellThrSet createThrSet(MapCellThrSetResponseDto thrSetDto);

    MapCellThrSetResponseDto createThrSetResponse(MapCellThrSetResponseDto dto);

    MapCellThrSet findThrSetById(Long id);

    MapCellThrSet findThrSet(StandardKpi standardKpi, Rat rat, Granularity granularity, boolean isAdmin);

    MapCellThrSetDto getThrSet(String standardKpiName, String ratName, String granularityName, boolean isAdmin);

    MapCellThrSetResponseDto getThrSetResponse(String standardKpiName, String ratName, String granularityName, boolean isAdmin);
}
