import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {AreaTypeDto} from '../../models/pulse/AreaTypeDto';

@Injectable({
  providedIn: 'root'
})
export class AreaTypeService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/area-types`;
  }

  getAllAreaTypes(){
    return this.http.get<Array<AreaTypeDto>>(`${this.baseUrl}`);
  }

}
