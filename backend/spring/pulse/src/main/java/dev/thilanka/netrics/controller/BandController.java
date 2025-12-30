package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.BandDto;
import dev.thilanka.netrics.service.BandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/bands")
@RequiredArgsConstructor
public class BandController {
    private final BandService bandService;

    @PostMapping
    public ResponseEntity<BandDto> createBand(@RequestBody @Valid BandDto dto){
        BandDto savedDto = bandService.createBand(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    public ResponseEntity<List<BandDto>> createBandList(@RequestBody @Valid List<BandDto> dtos){
        List<BandDto> savedDtos = bandService.createBands(dtos);
        return new ResponseEntity<>(savedDtos, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BandDto>> getAllBands(){
        List<BandDto> allBands = bandService.getAll();
        return ResponseEntity.ok(allBands);
    }

    @GetMapping("{name}")
    public ResponseEntity<BandDto> getByName(@PathVariable("name") String name){
        return ResponseEntity.ok(bandService.getByName(name));
    }

    @GetMapping("rat")
    public ResponseEntity<List<BandDto>> getByRatName(@RequestParam("ratName")String ratName){
        return ResponseEntity.ok(bandService.getBandsByRat(ratName));
    }
}
