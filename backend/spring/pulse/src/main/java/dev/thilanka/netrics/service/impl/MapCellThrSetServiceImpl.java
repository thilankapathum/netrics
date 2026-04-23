package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.common.exception.UnauthorizedAccessException;
import dev.thilanka.netrics.dto.MapCellThrSetDto;
import dev.thilanka.netrics.dto.MapCellThrSetResponseDto;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.MapCellThrSet;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.MapCellThrSetRepository;
import dev.thilanka.netrics.repository.RatRepository;
import dev.thilanka.netrics.service.GranularityService;
import dev.thilanka.netrics.service.MapCellThrSetService;
import dev.thilanka.netrics.service.RatService;
import dev.thilanka.netrics.service.StandardKpiService;
import dev.thilanka.netrics.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapCellThrSetServiceImpl implements MapCellThrSetService {
    private final MapCellThrSetRepository mapCellThrSetRepository;
    private final RatService ratService;
    private final GranularityService granularityService;
    private final StandardKpiService standardKpiService;
    private final Mapper mapper;
    private final SecurityUtils securityUtils;

    @Override
    public MapCellThrSet createThrSet(MapCellThrSet thrSet) {
        return mapCellThrSetRepository.save(thrSet);
    }

    @Override
    public MapCellThrSetDto createThrSet(MapCellThrSetDto thrSetDto) {

        Rat rat = ratService.findRatByName(thrSetDto.ratName());
        Granularity granularity = granularityService.findGranularityByName(thrSetDto.granularityName());
        StandardKpi standardKpi = standardKpiService.findByKpiName(thrSetDto.standardKpiName(), rat);
        boolean isAdmin = securityUtils.hasRole("PULSE_DELETE");

        if (!securityUtils.getCurrentUserId().isBlank()) {
            String userId = securityUtils.getCurrentUserId();
            MapCellThrSet thrSet = MapCellThrSet.builder()
                    .standardKpi(standardKpi)
                    .granularity(granularity)
                    .rat(rat)
                    .userId(userId)
                    .isAdmin(thrSetDto.isAdmin() && isAdmin)    //-- validate isAdmin with user's actual roles
                    .isActive(thrSetDto.isActive())
                    .isDeleted(thrSetDto.isDeleted())
                    .build();

            MapCellThrSet savedThrSet = mapCellThrSetRepository.save(thrSet);

            return mapper.mapCellThrSetToDto(savedThrSet);
        } else throw new UnauthorizedAccessException("User unavailable");

    }

    @Override
    public MapCellThrSetResponseDto createThrSet(MapCellThrSetResponseDto dto) {
        Rat rat = ratService.findRatByName(dto.ratName());
        Granularity granularity = granularityService.findGranularityByName(dto.granularityName());
        StandardKpi standardKpi = standardKpiService.findByKpiName(dto.standardKpiName(), rat);
        boolean isAdmin = securityUtils.hasRole("PULSE_DELETE");

        if (!securityUtils.getCurrentUserId().isBlank()) {
            String userId = securityUtils.getCurrentUserId();
            MapCellThrSet thrSet = MapCellThrSet.builder()
                    .standardKpi(standardKpi)
                    .granularity(granularity)
                    .rat(rat)
                    .userId(userId)
                    .isAdmin(dto.isAdmin() && isAdmin)    //-- validate isAdmin with user's actual roles
                    .isActive(true)     //-- Set initially active
                    .isDeleted(false)   //-- Set not-deleted
                    .build();

            MapCellThrSet savedThrSet = mapCellThrSetRepository.save(thrSet);

            return mapper.mapCellThrSetToResponseDto(savedThrSet);
        } else throw new UnauthorizedAccessException("User unavailable");
    }

    @Override
    public MapCellThrSet findThrSetById(Long id) {
        return mapCellThrSetRepository.findThrSetById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Threshold Set", "id", id));
    }

    @Override
    public MapCellThrSet findThrSet(StandardKpi standardKpi, Rat rat, Granularity granularity, boolean isAdmin) {

        if (isAdmin) {
            return mapCellThrSetRepository.findAdminThrSet(standardKpi.getId(), rat.getId(), granularity.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Threshold Set", "Standard KPI, RAT, Granularity",
                            standardKpi.getKpiName() + " - " + rat.getName() + " - " + granularity.getName()));
        } else {
            String userId = securityUtils.getCurrentUserId();
            return mapCellThrSetRepository.findUserThrSet(standardKpi.getId(), rat.getId(), granularity.getId(), userId )
                    .orElseThrow(() -> new ResourceNotFoundException(
                    "Threshold Set", "User", userId));
        }
    }

    @Override
    public MapCellThrSetDto getThrSet(String standardKpiName, String ratName, String granularityName, boolean isAdmin) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, rat);
        return mapper.mapCellThrSetToDto(findThrSet(standardKpi, rat, granularity, isAdmin));
    }

    @Override
    public MapCellThrSetResponseDto getThrSetResponse(String standardKpiName, String ratName, String granularityName, boolean isAdmin) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, rat);
        return mapper.mapCellThrSetToResponseDto(findThrSet(standardKpi, rat, granularity, isAdmin));
    }
}
