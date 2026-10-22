import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {Observable} from 'rxjs';

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
}
