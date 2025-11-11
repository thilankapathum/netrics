import { Injectable } from '@angular/core';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UrlService {

  baseUrl:string = environment.baseUrl;
  authUrl:string = environment.authUrl;
  pulseUrl: string = `${this.baseUrl}/api/v1/pulse`;

  getPulseUrl(){
    return this.pulseUrl;
  }

  getBaseUrl(){
    return this.baseUrl;
  }

  getAuthUrl(){
    return this.authUrl;
  }

}
