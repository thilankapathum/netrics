import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {Observable} from 'rxjs';
import {EngineeringOverviewDto} from '../../models/beam/EngineeringOverviewDto';
import {OperationLogDto} from '../../models/beam/OperationLogDto';

export type EngineeringExportLevel = 'SITE' | 'SECTOR' | 'ANTENNA' | 'TILT';

@Injectable({
  providedIn: 'root'
})
export class EngineeringParameterService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/engineering-parameters`;
  }

  /** CSV down to the given level; no siteCode exports every site */
  export(level: EngineeringExportLevel, siteCode?: string): Observable<Blob> {
    const params: Record<string, string> = siteCode ? {level, siteCode} : {level};
    return this.http.get(`${this.baseUrl}/export`, {params, responseType: 'blob'});
  }

  overview(): Observable<EngineeringOverviewDto> {
    return this.http.get<EngineeringOverviewDto>(`${this.baseUrl}/overview`);
  }

  /** Latest site/sector/antenna/tilt changes, newest first */
  changes(page: number, size: number): Observable<OperationLogDto[]> {
    return this.http.get<OperationLogDto[]>(`${this.baseUrl}/changes`, {params: {page, size}});
  }
}
