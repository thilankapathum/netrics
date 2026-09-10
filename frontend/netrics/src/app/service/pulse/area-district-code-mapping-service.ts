import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {AreaDistrictCodeMappingDto} from '../../models/pulse/AreaDistrictCodeMappingDto';

@Injectable({
  providedIn: 'root'
})
export class AreaDistrictCodeMappingService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/area-district-code`;
  }

  create(mapping: AreaDistrictCodeMappingDto){
    return this.http.post<AreaDistrictCodeMappingDto>(`${this.baseUrl}`, mapping);
  }

  getAll(){
    return this.http.get<Array<AreaDistrictCodeMappingDto>>(`${this.baseUrl}`);
  }

  delete(id: number){
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

}
