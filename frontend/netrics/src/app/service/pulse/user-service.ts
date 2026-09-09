import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {UserDto} from '../../models/pulse/UserDto';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/users`;
  }

  getAllUsers() {
    return this.http.get<Array<UserDto>>(this.baseUrl);
  }

}
