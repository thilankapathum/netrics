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

  create(dto: OperatorDto): Observable<OperatorDto> {
    return this.http.post<OperatorDto>(this.baseUrl, dto);
  }

  update(dto: OperatorDto): Observable<OperatorDto> {
    return this.http.put<OperatorDto>(this.baseUrl, dto);
  }

  delete(name: string, force = false): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${name}`, {params: {force}});
  }

  getDependents(name: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/${name}/dependents`);
  }
}
