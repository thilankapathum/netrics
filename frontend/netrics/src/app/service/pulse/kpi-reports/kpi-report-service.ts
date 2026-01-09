import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {AreaDto} from '../../../models/pulse/AreaDto';

@Injectable({
  providedIn: 'root'
})
export class KpiReportService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/kpi-reports`;
  }

  exportSiteWiseReportByKpiAndDate(ratName:string, granularityName:string, standardKpiName:string, startDate:string, endDate:string, areaName:string) {
    return this.http.get(`${this.baseUrl}/export/site-kpi-date`, {responseType: 'blob', params: {ratName, granularityName, standardKpiName, startDate, endDate, areaName}});
  }

}
