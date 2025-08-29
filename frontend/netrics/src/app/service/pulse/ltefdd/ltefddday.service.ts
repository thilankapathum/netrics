import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BasicKpiSnapshot} from '../../../models/pulse/BasicKpiSnapshot';
import {WorstCell} from '../../../models/pulse/WorstCell';
import {KpiDataDto} from '../../../models/pulse/KpiDataDto';
import {KpiTrendDto} from '../../../models/pulse/KpiTrendDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LtefdddayService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/ltefdd/kpiday`;
  }

  getBasicKpiSnapshot(kpiName: string, period: string, districtName:string) {
    return this.http.get<BasicKpiSnapshot>(`${this.baseUrl}/snapshot/basic-kpi`,
      { params : {kpiName, period, districtName} });
  }

  // getWorstCellsByKpiX(kpiName: string, granularity: string, count: number) {
  //   return this.http.get<Array<WorstCell>>(`${this.baseUrl}/worst-cells?kpiName=${kpiName}&period=${granularity}&count=${count}`);
  // }

  getWorstCellsByKpi(kpiName: string, period: string, districtName:string, page: number, size: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/worst-cells-page`, {
      params: { kpiName, period, districtName, page, size }
    });
  }

  getDataByKpiAndCell(kpiName:string, cellName:string, period: string) {
    return this.http.get<Array<KpiDataDto>>(`${this.baseUrl}/cell`,
      { params : {kpiName, cellName, period} });
  }

  getDataByKpiLabelAndCell(kpiLabel:string, cellName:string, period: string) {
    return this.http.get<Array<KpiDataDto>>(`${this.baseUrl}/cell-label`,
      { params: {kpiLabel, cellName, period} });
  }

  getDataByKpi(kpiName:string, period: string, districtName:string) {
    return this.http.get<Array<KpiTrendDto>>(`${this.baseUrl}/kpi`,
      {params : {kpiName, period, districtName} });
  }
}
