import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {DistrictDto} from '../../../models/pulse/DistrictDto';

@Injectable({
  providedIn: 'root'
})
export class DistrictService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/districts`;
  }

  getAllDistricts(){
    return this.http.get<Array<DistrictDto>>(`${this.baseUrl}`);
  }

}
