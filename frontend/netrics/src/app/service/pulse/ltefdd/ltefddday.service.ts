import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BasicKpiSnapshot} from '../../../models/pulse/BasicKpiSnapshot';

@Injectable({
  providedIn: 'root'
})
export class LtefdddayService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/ltefdd/kpiday`;
  }

  getBasicKpiSnapshot(kpiName: string, period: string) {
    return this.http.get<BasicKpiSnapshot>(`${this.baseUrl}/snapshot/basic-kpi?kpiName=${kpiName}&period=${period}`);
  }
}
