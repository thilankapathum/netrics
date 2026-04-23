package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.MapCellThrSetDto;
import dev.thilanka.netrics.entity.MapCellThrSet;
import dev.thilanka.netrics.repository.MapCellThrSetRepository;
import dev.thilanka.netrics.service.MapCellThrSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapCellThrSetServiceImpl implements MapCellThrSetService {
    private final MapCellThrSetRepository mapCellThrSetRepository;

    @Override
    public MapCellThrSet createThrSet(MapCellThrSet thrSet) {
        return null;
    }

    @Override
    public MapCellThrSetDto createThrSet(MapCellThrSetDto thrSetDto) {
        return null;
    }
}
