import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {StandardKpiDto} from '../../../models/pulse/StandardKpiDto';

@Injectable({
  providedIn: 'root'
})
export class LtefddstandardkpiService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/ltefdd/standardkpi`;
  }

  getAllStandardKpi(){
    return this.http.get<Array<StandardKpiDto>>(this.baseUrl);
  }

}
