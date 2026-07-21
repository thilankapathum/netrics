package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AlarmFilter;
import dev.thilanka.netrics.dto.AlarmsDto;
import dev.thilanka.netrics.dto.CellAlarmDto;
import dev.thilanka.netrics.dto.PagedResponse;

import java.util.List;

public interface AlarmService {

    List<CellAlarmDto> getAlarmsByCell(String cellName, String period);

    PagedResponse<AlarmsDto> getAlarms(AlarmFilter filter, int page, int size);
}
