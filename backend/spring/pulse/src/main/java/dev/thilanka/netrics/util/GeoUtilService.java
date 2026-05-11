package dev.thilanka.netrics.util;

import org.springframework.stereotype.Service;

@Service
public class GeoUtilService {

    public boolean isValidAzimuth(Integer azimuth) {
        return azimuth >= 0 && azimuth <= 360;
    }

    public boolean isValidBeamwidth(Integer beamwidth) {
        return beamwidth >= 0 && beamwidth <= 360;
    }
}
