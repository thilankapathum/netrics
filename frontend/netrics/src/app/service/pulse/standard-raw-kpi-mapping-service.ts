import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';

@Injectable({
  providedIn: 'root'
})
export class StandardRawKpiMappingService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/standard-raw-kpimap`;
  }

  isMappingAvailable(ratName: string, standardKpiName: string) {
    return this.http.get<boolean>(`${this.baseUrl}/is-available`, {
      params: {
        ratName: ratName,
        standardKpiName: standardKpiName
      }
    });
  }

}
