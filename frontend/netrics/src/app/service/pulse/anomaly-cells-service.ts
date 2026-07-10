import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {AnomalyCellDto, PagedResponse} from '../../models/pulse/AnomalyCellDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AnomalyCellsService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/anomaly`;
  }

  getAnomalyCells(params: {
    period: string;
    areaName: string;
    ratName: string;
    granularityName: string;
    severity?: string | null;
    sortBy: string;
    sortDir: string;
    page: number;
    pageSize: number;
  }): Observable<PagedResponse<AnomalyCellDto>> {
    let httpParams = new HttpParams()
      .set('period', params.period)
      .set('areaName', params.areaName)
      .set('ratName', params.ratName)
      .set('granularityName', params.granularityName)
      .set('sortBy', params.sortBy)
      .set('sortDir', params.sortDir)
      .set('page', params.page.toString())
      .set('pageSize', params.pageSize.toString());

    if (params.severity) {
      httpParams = httpParams.set('severity', params.severity);
    }

    return this.http.get<PagedResponse<AnomalyCellDto>>(`${this.baseUrl}/cells`, { params: httpParams });
  }

}
