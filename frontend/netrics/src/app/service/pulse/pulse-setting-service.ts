import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {UserAreaMappingDto} from '../../models/pulse/UserAreaMappingDto';

@Injectable({
  providedIn: 'root'
})
export class PulseSettingService {

  private readonly userAreaMappingUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.userAreaMappingUrl = `${this.urlService.getPulseUrl()}/user-area`;
  }

  createUserAreaMapping(userAreaMapping:UserAreaMappingDto){
    return this.http.post<UserAreaMappingDto>(`${this.userAreaMappingUrl}`,userAreaMapping);
  }

}
