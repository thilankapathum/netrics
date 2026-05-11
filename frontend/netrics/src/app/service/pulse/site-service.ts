import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {SiteDto} from '../../models/pulse/SiteDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SiteService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/sites`;
  }

  createSite(site: SiteDto): Observable<SiteDto> {
    return this.http.post<SiteDto>(`${this.baseUrl}`, site);
  }

  createSiteList(json: string) {
    const body = JSON.parse(json);
    return this.http.post<Array<SiteDto>>(`${this.baseUrl}/list`,
      body,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
  }

  getSiteBySiteCode(siteCode: string): Observable<SiteDto> {
    return this.http.get<SiteDto>(`${this.baseUrl}`, {params: {siteCode: siteCode}});
  }

  updateSite(site: SiteDto): Observable<SiteDto> {
    return this.http.put<SiteDto>(`${this.baseUrl}`, site);
  }

  searchSites(search: string): Observable<SiteDto[]> {
    const params = new HttpParams().set('search', search);
    return this.http.get<SiteDto[]>(`${this.baseUrl}/search`, {params});
  }

  exportSitesWithMissingInfo() {
    return this.http.get(`${this.baseUrl}/missing/export`, {responseType: 'blob'});
  }

  exportAllSites() {
    return this.http.get(`${this.baseUrl}/all/export`, {responseType: 'blob'});
  }

  importSitesWithCorrectedInfo(file: File): Observable<Blob> {
    const url = `${this.baseUrl}/missing/import`;
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(url, formData, {responseType: 'blob'});
  }

  getSiteCountWithMissingInfo() {
    return this.http.get<number>(`${this.baseUrl}/missing/count`);
  }

}
