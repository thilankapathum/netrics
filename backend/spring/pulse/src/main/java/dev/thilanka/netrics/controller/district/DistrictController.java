package dev.thilanka.netrics.controller.district;

import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.service.DistrictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    ResponseEntity<List<DistrictDto>> getAll(){
        return new ResponseEntity<>(districtService.getAll(), HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<DistrictDto> createDistrict(@RequestBody @Valid DistrictDto dto){
        return new ResponseEntity<>(districtService.createDistrict(dto),HttpStatus.CREATED);
    }

    @PostMapping("list")
    ResponseEntity<List<DistrictDto>> createDistrictList(@RequestBody @Valid List<DistrictDto> dtos){
        return new ResponseEntity<>(districtService.createDistrictList(dtos),HttpStatus.CREATED);
    }


}
