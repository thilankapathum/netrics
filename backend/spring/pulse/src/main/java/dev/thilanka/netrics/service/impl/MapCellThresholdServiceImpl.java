package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.repository.MapCellThresholdRepository;
import dev.thilanka.netrics.service.MapCellThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapCellThresholdServiceImpl implements MapCellThresholdService {
    private final MapCellThresholdRepository mapCellThresholdRepository;
}
