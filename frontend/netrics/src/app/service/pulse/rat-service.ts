import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {RatDto} from '../../models/pulse/RatDto';

@Injectable({
  providedIn: 'root'
})
export class RatService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/rat`;
  }

  getAllRats() {
    return this.http.get<Array<RatDto>>(this.baseUrl);
  }

  findByName(ratName: string) {
    return this.http.get<RatDto>(`${this.baseUrl}/name/${ratName}`);
  }

  findByLabel(label: string) {
    return this.http.get<RatDto>(`${this.baseUrl}/label/${label}`);
  }
}
