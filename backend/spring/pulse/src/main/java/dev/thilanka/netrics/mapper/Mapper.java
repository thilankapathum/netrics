package dev.thilanka.netrics.mapper;

import dev.thilanka.netrics.dto.OssDto;
import dev.thilanka.netrics.entity.Oss;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Mapper {
    public OssDto ossToDto(Oss oss){
        OssDto ossDto = new OssDto(oss.getOssName(),oss.getIdentifier(),oss.getVendor());
        return ossDto;
    }

    public Oss ossDtoToOss(OssDto ossDto){
        Oss oss = Oss.builder()
                .ossName(ossDto.ossName())
                .vendor(ossDto.vendor())
                .identifier(ossDto.identifier())
                .build();
        return oss;
    }
}
