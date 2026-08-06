import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {KpiMappingToOssDto} from '../../models/pulse/KpiMappingToOssDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KpimappingtoossService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/kpimappingtooss`;
  }

  getAllKpiMappings(ratName: string): Observable<KpiMappingToOssDto[]> {
    return this.http.get<Array<KpiMappingToOssDto>>(this.baseUrl, { params: { ratName } });
  }

  createKpiMapping(dto: KpiMappingToOssDto): Observable<KpiMappingToOssDto> {
    return this.http.post<KpiMappingToOssDto>(this.baseUrl, dto);
  }
}
