package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.AlarmFilter;
import dev.thilanka.netrics.dto.AlarmsDto;
import dev.thilanka.netrics.dto.CellAlarmDto;
import dev.thilanka.netrics.dto.PagedResponse;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.repository.AlarmRepository;
import dev.thilanka.netrics.service.AlarmService;
import dev.thilanka.netrics.service.AreaService;
import dev.thilanka.netrics.service.DateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {
    private final AlarmRepository alarmRepository;
    private final DateService dateService;
    private final AreaService areaService;

    @Override
    public List<CellAlarmDto> getAlarmsByCell(String cellName, String period) {
        LocalDateTime startTime = dateService.getStartDate(LocalDateTime.now(), period);
        return alarmRepository.getAlarmsByCell(cellName, startTime);
    }

    @Override
    public PagedResponse<AlarmsDto> getAlarms(AlarmFilter filter, int page, int size) {
        LocalDateTime startTime = dateService.getStartDate(LocalDateTime.now(), filter.period());
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 25 : Math.min(size, 200);
        long offset = (long) safePage * safeSize;

        Area area = areaService.findAreaByName(filter.areaName());

        List<AlarmsDto> content = alarmRepository.findAlarms(
                startTime, filter.nodeName(), filter.severity(), filter.alarmType(),
                filter.alarmName(), filter.ackState(), filter.clearState(),
                filter.alarmSource(), area.getId(), safeSize, offset);

        long total = alarmRepository.countAlarms(
                startTime, filter.nodeName(), filter.severity(), filter.alarmType(),
                filter.alarmName(), filter.ackState(), filter.clearState(),
                filter.alarmSource(), area.getId());

        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) total / safeSize);

        log.info("Alarms query: {} results, page {}/{}", total, safePage, totalPages);
        return new PagedResponse<>(content, total, totalPages, safePage, safeSize);
    }
}
