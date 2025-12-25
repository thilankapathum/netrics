package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/sites")
@RequiredArgsConstructor
public class SiteController {
    private final SiteService siteService;


    @PostMapping
    public ResponseEntity<SiteDto> createSite(@RequestBody @Valid SiteDto dto){
        SiteDto savedDto = siteService.createSite(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("list")
    public ResponseEntity<List<SiteDto>> createSiteList(@RequestBody @Valid List<SiteDto> dtos){
        List<SiteDto> savedDtos = siteService.createSites(dtos);
        return new ResponseEntity<>(savedDtos,HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<SiteDto> getSiteBySiteCode(@RequestParam("siteCode") String siteCode){
        SiteDto dto = siteService.getBySiteCode(siteCode);
        return ResponseEntity.ok(dto);
    }

}
