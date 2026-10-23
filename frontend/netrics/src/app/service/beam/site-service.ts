import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {SiteDto} from '../../models/beam/SiteDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SiteService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/sites`;
  }

  getBySiteCode(siteCode: string): Observable<SiteDto> {
    return this.http.get<SiteDto>(this.baseUrl, {params: {siteCode}});
  }

  updateSite(site: SiteDto): Observable<SiteDto> {
    return this.http.put<SiteDto>(this.baseUrl, site);
  }
}
