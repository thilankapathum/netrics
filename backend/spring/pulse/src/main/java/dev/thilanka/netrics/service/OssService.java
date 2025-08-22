package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.OssDto;

import java.util.List;

public interface OssService {

    List<OssDto> getAllOss();

    OssDto createOss(OssDto ossDto);

    List<OssDto> createMultipleOss(List<OssDto> ossDtos);
}
