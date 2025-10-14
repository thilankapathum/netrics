package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.RatDto;
import dev.thilanka.netrics.entity.Rat;

import java.util.List;

public interface RatService {

    List<RatDto> getAll();

    List<Rat> findAll();

    RatDto getRatDtoByName(String name);

    RatDto getRatDtoByLabel(String label);

    RatDto createRat(RatDto dto);

    Rat findRatByName(String name);

    List<RatDto> createRatList(List<RatDto> ratDtos);
}
