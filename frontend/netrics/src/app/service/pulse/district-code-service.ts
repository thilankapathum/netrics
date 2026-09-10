import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {DistrictCodeDto} from '../../models/pulse/DistrictCodeDto';

@Injectable({
  providedIn: 'root'
})
export class DistrictCodeService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/district-codes`;
  }

  getAll(){
    return this.http.get<Array<DistrictCodeDto>>(`${this.baseUrl}`);
  }

}
