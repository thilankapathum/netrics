import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';

@Injectable({
  providedIn: 'root'
})
export class CacheService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/cache`;
  }

  evictAllCaches() {
    return  this.http.post(`${this.baseUrl}/evict-all`, {responseType: "text"});
  }

  evictByRatName(ratName: string) {
    return  this.http.post(`${this.baseUrl}/evict`, null, {params: {ratName}, responseType: "text"});
  }

  evictByRatAndGranularity(ratName: string, granularityName: string) {
    return  this.http.post(`${this.baseUrl}/evict-rat-granularity`, null, {params: {ratName, granularityName}, responseType: "text"});
  }

}
