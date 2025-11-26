import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {AreaDto} from '../../../models/pulse/AreaDto';
import {WorstCellsDashboardDto} from '../../../models/pulse/WorstCellsDashboardDto';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/worst-cell-dashboard`;
  }

  getTimestamps(kpiName: string, period: string, areaName: string, ratName: string,) {
    return this.http.get<Array<Date>>(`${this.baseUrl}/timestamps`, {params: {kpiName, period, areaName, ratName}});
  }

  getWorstCellsByKpiAndArea(timestamp: string, kpiName: string, period: string, areaName: string, excludeZeroes: boolean, ratName: string) {
    return this.http.get<Array<WorstCellsDashboardDto>>(`${this.baseUrl}`, {
      params: {
        timestamp,
        kpiName,
        period,
        areaName,
        excludeZeroes,
        ratName
      }
    })
  }

}
