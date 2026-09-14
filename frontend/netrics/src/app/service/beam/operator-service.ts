import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {OperatorDto} from '../../models/beam/OperatorDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OperatorService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/operators`;
  }

  getAll(): Observable<OperatorDto[]> {
    return this.http.get<OperatorDto[]>(this.baseUrl);
  }
}
