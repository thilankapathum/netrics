package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.repository.ElectricalTiltRepository;
import dev.thilanka.beam.service.ElectricalTiltService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElectricalTiltServiceImpl implements ElectricalTiltService {
    private final ElectricalTiltRepository electricalTiltRepository;
}
