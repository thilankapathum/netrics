package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.repository.DistrictCodeRepository;
import dev.thilanka.netrics.service.DistrictCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistrictCodeServiceImpl implements DistrictCodeService {
    private final DistrictCodeRepository districtCodeRepository;
}
