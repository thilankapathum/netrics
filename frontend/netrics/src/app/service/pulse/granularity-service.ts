import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {RatDto} from '../../models/pulse/RatDto';
import {GranularityDto} from '../../models/pulse/GranularityDto';

@Injectable({
  providedIn: 'root'
})
export class GranularityService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/granularity`;
  }

  getAllGranularities() {
    return this.http.get<Array<GranularityDto>>(this.baseUrl);
  }
}
