package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.GranularityDto;
import dev.thilanka.netrics.entity.Granularity;

import java.util.List;

public interface GranularityService {

    GranularityDto createGranularity(GranularityDto dto);

    List<GranularityDto> createGranularityList(List<GranularityDto> dtos);

    Granularity findGranularityByName(String name);

    GranularityDto getGranularityByName(String name);

    Granularity findGranularityById(Long id);

    GranularityDto getGranularityById(Long id);

    List<Granularity> findAll();

    List<GranularityDto> getAll();
}
