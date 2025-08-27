import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BasicKpiDto} from '../../../models/pulse/BasicKpiDto';

@Injectable({
  providedIn: 'root'
})
export class LtefddbasickpiService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/ltefdd/basickpi`;
  }

  getAllBasicKpi() {
    return this.http.get<Array<BasicKpiDto>>(this.baseUrl);
  }
}
