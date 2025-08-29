package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.repository.DistrictRepository;
import dev.thilanka.netrics.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {
    private final DistrictRepository districtRepository;
}
