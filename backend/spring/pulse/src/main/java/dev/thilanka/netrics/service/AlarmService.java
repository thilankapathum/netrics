package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AlarmFilter;
import dev.thilanka.netrics.dto.AlarmsDto;
import dev.thilanka.netrics.dto.CellAlarmDto;
import dev.thilanka.netrics.dto.PagedResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmService {

    List<CellAlarmDto> getAlarmsByCell(String cellName, String period);

    List<CellAlarmDto> getAlarmsByCell(String cellName, LocalDateTime startDate, String granularityName);

    PagedResponse<AlarmsDto> getAlarms(AlarmFilter filter, int page, int size);
}
