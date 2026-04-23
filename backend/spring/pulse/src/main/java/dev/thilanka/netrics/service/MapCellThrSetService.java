package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.MapCellThrSetDto;
import dev.thilanka.netrics.entity.MapCellThrSet;

public interface MapCellThrSetService {

    MapCellThrSet createThrSet(MapCellThrSet thrSet);

    MapCellThrSetDto createThrSet(MapCellThrSetDto thrSetDto);
}
