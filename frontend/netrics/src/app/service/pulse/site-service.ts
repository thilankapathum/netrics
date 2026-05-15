import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {SiteDto} from '../../models/pulse/SiteDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SiteService {
  private readonly getUrl: string;
  private readonly postUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.getUrl = `${this.urlService.getPulseUrl()}/sites`;
    this.postUrl = `${this.urlService.getBeamUrl()}/sites`;

  }

  createSite(site: SiteDto): Observable<SiteDto> {
    return this.http.post<SiteDto>(`${this.postUrl}`, site);
  }

  createSiteList(json: string) {
    const body = JSON.parse(json);
    return this.http.post<Array<SiteDto>>(`${this.postUrl}/list`,
      body,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
  }

  getSiteBySiteCode(siteCode: string): Observable<SiteDto> {
    return this.http.get<SiteDto>(`${this.getUrl}`, {params: {siteCode: siteCode}});
  }

  updateSite(site: SiteDto): Observable<SiteDto> {
    return this.http.put<SiteDto>(`${this.postUrl}`, site);
  }

  searchSites(search: string): Observable<SiteDto[]> {
    const params = new HttpParams().set('search', search);
    return this.http.get<SiteDto[]>(`${this.getUrl}/search`, {params});
  }

  exportSitesWithMissingInfo() {
    return this.http.get(`${this.postUrl}/missing/export`, {responseType: 'blob'});
  }

  exportAllSites() {
    return this.http.get(`${this.postUrl}/all/export`, {responseType: 'blob'});
  }

  importSitesWithCorrectedInfo(file: File): Observable<Blob> {
    const url = `${this.postUrl}/missing/import`;
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(url, formData, {responseType: 'blob'});
  }

  getSiteCountWithMissingInfo() {
    return this.http.get<number>(`${this.postUrl}/missing/count`);
  }

}
