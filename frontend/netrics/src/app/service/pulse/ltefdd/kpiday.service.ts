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
export class KpidayService {
  private readonly baseUrl: string;
  // private readonly ratName:string = 'ltefdd';

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/kpiday`;
  }

  getBasicKpiSnapshot(kpiName: string, period: string, districtName:string, ratName:string) {
    // const ratName = this.ratName;
    return this.http.get<BasicKpiSnapshot>(`${this.baseUrl}/snapshot/basic-kpi`,
      { params : {kpiName, period, districtName, ratName} });
  }

  getWorstCellsByKpi(kpiName: string, period: string, districtName:string, excludeZeroes:boolean, ratName:string): Observable<any> {
    // const ratName: string = this.ratName;
    return this.http.get(`${this.baseUrl}/worst-cells`,{ params: { kpiName, period, districtName, excludeZeroes, ratName } });
  }

  getDataByKpiAndCell(kpiName:string, cellName:string, period: string, ratName:string): Observable<any> {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiDataDto>>(`${this.baseUrl}/cell`,
      { params : {kpiName, cellName, period, ratName} });
  }

  getDataByKpiLabelAndCell(kpiLabel:string, cellName:string, period: string, ratName:string) {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiDataDto>>(`${this.baseUrl}/cell-label`,
      { params: {kpiLabel, cellName, period, ratName } });
  }

  getDataByKpi(kpiName:string, period: string, districtName:string, ratName:string) {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiTrendDto>>(`${this.baseUrl}/kpi`,
      {params : {kpiName, period, districtName, ratName} });
  }
}
