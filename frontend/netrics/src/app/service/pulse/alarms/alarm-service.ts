import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {AreaDto} from '../../../models/pulse/AreaDto';
import {AlarmFilter, AlarmsDto, CellAlarmDto} from '../../../models/pulse/alarms/AlarmDto';
import {PagedResponse} from '../../../models/pulse/AnomalyCellDto';

@Injectable({
  providedIn: 'root'
})
export class AlarmService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/alarms/details`;
  }

  getAlarmsByCell(cellName: string, period: string) {
    return this.http.get<Array<CellAlarmDto>>(`${this.baseUrl}/cell`, {params: {cellName, period}});
  }

  getAlarms(filter: AlarmFilter, page: number, size: number) {
    let params = new HttpParams()
      .set('period', filter.period)
      .set('page', page)
      .set('size', size);

    if (filter.nodeName) params = params.set('nodeName', filter.nodeName);
    if (filter.severity) params = params.set('severity', filter.severity);
    if (filter.alarmType) params = params.set('alarmType', filter.alarmType);
    if (filter.alarmName) params = params.set('alarmName', filter.alarmName);
    if (filter.ackState) params = params.set('ackState', filter.ackState);
    if (filter.clearState) params = params.set('clearState', filter.clearState);
    if (filter.alarmSource) params = params.set('alarmSource', filter.alarmSource);
    if (filter.areaName != null) params = params.set('areaName', filter.areaName);

    return this.http.get<PagedResponse<AlarmsDto>>(this.baseUrl, { params });
  }

}
