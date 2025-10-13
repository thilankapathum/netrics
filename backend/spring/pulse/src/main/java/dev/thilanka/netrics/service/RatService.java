package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.RatDto;

import java.util.List;

public interface RatService {

    List<RatDto> getAll();

    RatDto getRatByName(String name);

    RatDto getRatByLabel(String label);

    RatDto createRat(RatDto dto);

    List<RatDto> createRatList(List<RatDto> ratDtos);
}
