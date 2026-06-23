package dev.thilanka.beam.common;

import dev.thilanka.beam.dto.AntennaDto;
import dev.thilanka.beam.dto.ManufacturerDto;
import dev.thilanka.beam.dto.OperatorDto;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.Manufacturer;
import dev.thilanka.beam.entity.Operator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Mapper {

    //--------  ANTENNA  -------------------------

    public AntennaDto toAntennaDto(Antenna antenna) {
        return new AntennaDto(
                antenna.getId(),
                antenna.getAntennaIndex(),
                antenna.getAzimuth(),
                antenna.getMechanicalTilt(),
                antenna.getAntennaHeight(),
                antenna.getAntennaType().getName(),
                antenna.getSector().getName(),
                antenna.getManufacturer().getName()
        );
    }

    //--------  OPERATOR  -------------------------

    public OperatorDto toOperatorDto(Operator operator) {
        return new OperatorDto(operator.getName());
    }

    //--------  MANUFACTURER  ---------------------

    public ManufacturerDto toManufacturerDto(Manufacturer manufacturer) {
        return new ManufacturerDto(manufacturer.getName());
    }

}
