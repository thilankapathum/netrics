import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {AreaDto} from '../../../models/pulse/AreaDto';
import {WorstCellsWithLatestDto} from '../../../models/pulse/WorstCellsWithLatestDto';
import {WorstCellCreationStatusDto} from '../../../models/pulse/WorstCellCreationStatusDto';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/worst-cell-dashboard`;
  }

  getTimestamps(kpiName: string, period: string, areaName: string, ratName: string, granularityName: string) {
    return this.http.get<Array<string>>(`${this.baseUrl}/timestamps`, {
      params: {
        kpiName,
        period,
        areaName,
        ratName,
        granularityName
      }
    });
  }

  getWorstCellsByKpiAndArea(timestamp: string, kpiName: string, period: string, areaName: string, excludeZeroes: boolean, ratName: string, granularityName: string) {
    return this.http.get<Array<WorstCellsWithLatestDto>>(`${this.baseUrl}`, {
      params: {
        timestamp,
        kpiName,
        period,
        areaName,
        excludeZeroes,
        ratName,
        granularityName
      }
    });
  }

  createWorstCellsByRatAndAreaType(period: string, areaType: string, date: string, ratName: string, granularityName: string) {
    return this.http.post(
      `${this.baseUrl}/areatype-rat`,
      null,
      {
        params: {
          period,
          areaType,
          date,
          ratName,
          granularityName
        }
      }
    );
  }

  getWorstCellCreationStatus(){
    return this.http.get<WorstCellCreationStatusDto>(`${this.baseUrl}/create-status`);
  }


}
