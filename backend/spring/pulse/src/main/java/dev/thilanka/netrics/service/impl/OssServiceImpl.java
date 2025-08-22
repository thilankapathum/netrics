package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.OssDto;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.OssRepository;
import dev.thilanka.netrics.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {
    private final OssRepository ossRepository;
    private final Mapper mapper;

    @Override
    public List<OssDto> getAllOss() {
        List<Oss> ossList = ossRepository.findAll();
        return ossList
                .stream()
                .map(mapper::ossToDto)
                .toList();
    }

    @Override
    public OssDto createOss(OssDto ossDto) {
        Oss oss = mapper.ossDtoToOss(ossDto);
        Oss savedOss = ossRepository.save(oss);
        return mapper.ossToDto(savedOss);
    }

    @Override
    public List<OssDto> createMultipleOss(List<OssDto> ossDtos) {

        List<OssDto> savedOss = new ArrayList<>();

        for (OssDto dto: ossDtos){
            OssDto savedDto = createOss(dto);
            savedOss.add(savedDto);
        }

        return savedOss;
    }
}
