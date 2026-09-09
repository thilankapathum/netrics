import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {AreaDto} from '../../models/pulse/AreaDto';

@Injectable({
  providedIn: 'root'
})
export class AreaService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/areas`;
  }

  getAreasByAreaTypes(areaTypeName:string){
    return this.http.get<Array<AreaDto>>(`${this.baseUrl}/area-type`, {params: {areaTypeName}});
  }

  createArea(area: AreaDto){
    return this.http.post<AreaDto>(`${this.baseUrl}`, area);
  }

}
