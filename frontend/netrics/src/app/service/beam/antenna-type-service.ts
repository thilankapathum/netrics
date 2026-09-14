import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {AntennaTypeDto} from '../../models/beam/AntennaTypeDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AntennaTypeService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/antenna-types`;
  }

  getAll(): Observable<AntennaTypeDto[]> {
    return this.http.get<AntennaTypeDto[]>(this.baseUrl);
  }
}
