package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.OssDto;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.OssRepository;
import dev.thilanka.netrics.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {
    private final OssRepository ossRepository;
    private final Mapper mapper;

    @Override
    public List<OssDto> getAllOss() {
        List<Oss> ossList = ossRepository.findAll();
        List<OssDto> ossDtoList = ossList.stream().map(o -> mapper.ossToDto(o)).toList();
        return ossDtoList;
    }

    @Override
    public OssDto createOss(OssDto ossDto) {
        Oss oss = mapper.ossDtoToOss(ossDto);
        Oss savedOss = ossRepository.save(oss);
        return mapper.ossToDto(savedOss);
    }
}
