import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BasicKpiSnapshot} from '../../../models/pulse/BasicKpiSnapshot';
import {WorstCell} from '../../../models/pulse/WorstCell';
import {KpiDataDto} from '../../../models/pulse/KpiDataDto';
import {KpiTrendDto} from '../../../models/pulse/KpiTrendDto';
import {Observable} from 'rxjs';
import {KpiDataWithOperandsDto} from '../../../models/pulse/KpiDataWithOperandsDto';
import {WorstCells} from '../../../models/pulse/WorstCells';

@Injectable({
  providedIn: 'root'
})
export class KpidayService {
  private readonly baseUrl: string;
  // private readonly ratName:string = 'ltefdd';

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/kpiday`;
  }

  getBasicKpiSnapshot(kpiName: string, period: string, districtName:string, ratName:string, granularityName:string) {
    // const ratName = this.ratName;
    return this.http.get<BasicKpiSnapshot>(`${this.baseUrl}/snapshot/basic-kpi`,
      { params : {kpiName, period, districtName, ratName, granularityName} });
  }

  getBasicKpiSnapshotByArea(kpiName: string, period: string, areaName:string, ratName:string, granularityName:string) {
    return this.http.get<BasicKpiSnapshot>(`${this.baseUrl}/snapshot/basic-kpi-area`,
      { params : {kpiName, period, areaName, ratName, granularityName} });
  }

  getWorstCellsByKpi(kpiName: string, period: string, districtName:string, excludeZeroes:boolean, ratName:string, granularityName:string): Observable<any> {
    // const ratName: string = this.ratName;
    return this.http.get(`${this.baseUrl}/worst-cells`,{ params: { kpiName, period, districtName, excludeZeroes, ratName, granularityName } });
  }

  getWorstCellsByKpiAndArea(kpiName: string, period: string, excludeZeroes:boolean, limit:number, areaName:string,  ratName:string, granularityName:string): Observable<any> {
    return this.http.get<Array<WorstCells>>(`${this.baseUrl}/worst-cells-area`,{ params: { kpiName, period, excludeZeroes, limit, areaName, ratName, granularityName } });
  }

  getWorstCellsByKpiAreaAndBand(kpiName: string, period: string, excludeZeroes:boolean, limit:number, areaName:string,  ratName:string, granularityName:string, bandName:string): Observable<any> {
    return this.http.get<Array<WorstCells>>(`${this.baseUrl}/worst-cells-area-band`,{ params: { kpiName, period, excludeZeroes, limit, areaName, ratName, granularityName, bandName } });
  }

  getDataByKpiAndCell(kpiName:string, cellName:string, period: string, ratName:string, granularityName:string): Observable<any> {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiDataDto>>(`${this.baseUrl}/cell`,
      { params : {kpiName, cellName, period, ratName, granularityName} });
  }

  getDataByKpiAndCellWithOperands(kpiName:string, cellName:string, period: string, ratName:string, granularityName:string): Observable<any> {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiDataWithOperandsDto>>(`${this.baseUrl}/cell-operands`,
      { params : {kpiName, cellName, period, ratName, granularityName} });
  }

  getDataByKpiLabelAndCell(kpiLabel:string, cellName:string, period: string, ratName:string, granularityName:string) {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiDataDto>>(`${this.baseUrl}/cell-label`,
      { params: {kpiLabel, cellName, period, ratName, granularityName } });
  }

  getDataByKpi(kpiName:string, period: string, districtName:string, ratName:string, granularityName:string) {
    // const ratName: string = this.ratName;
    return this.http.get<Array<KpiTrendDto>>(`${this.baseUrl}/kpi`,
      {params : {kpiName, period, districtName, ratName, granularityName} });
  }

  getDataByKpiAndArea(kpiName:string, period: string, areaName:string, ratName:string, granularityName:string) {
    return this.http.get<Array<KpiTrendDto>>(`${this.baseUrl}/kpi-area`,
      {params : {kpiName, period, areaName, ratName, granularityName} });
  }

  getDataByKpiAreaAndBand(kpiName:string, period: string, areaName:string, ratName:string, granularityName:string, bandName:string) {
    return this.http.get<Array<KpiTrendDto>>(`${this.baseUrl}/kpi-area-band`,
      {params : {kpiName, period, areaName, ratName, granularityName, bandName} });
  }
}
