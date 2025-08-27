import { Injectable } from '@angular/core';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UrlService {

  baseUrl:string = environment.baseUrl;
  pulseUrl: string = `${this.baseUrl}/api/v1/pulse`;

  getPulseUrl(){
    return this.pulseUrl;
  }

}
