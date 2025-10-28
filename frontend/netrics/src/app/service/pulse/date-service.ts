import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {DateRangeDto} from '../../models/pulse/DateRangeDto';

@Injectable({
  providedIn: 'root'
})
export class DateService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/dates`;
  }

  getLatestDateRange(period:string, ratName:string){
      return this.http.get<DateRangeDto>(`${this.baseUrl}`,{params:{period:period,ratName:ratName}});
  }

}
