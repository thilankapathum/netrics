package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.OssDto;
import dev.thilanka.netrics.service.OssService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/oss")
@RequiredArgsConstructor
public class OssController {
    private final OssService ossService;

    @GetMapping
    public ResponseEntity<List<OssDto>> getAllOss(){
        List<OssDto> ossDtoList = ossService.getAllOss();
        return new ResponseEntity<>(ossDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OssDto> createOss(@RequestBody @Valid OssDto ossDto){
        OssDto savedOssDto = ossService.createOss(ossDto);
        return new ResponseEntity<>(savedOssDto, HttpStatus.CREATED);
    }
}
