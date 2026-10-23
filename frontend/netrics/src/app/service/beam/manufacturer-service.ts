import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {ManufacturerDto} from '../../models/beam/ManufacturerDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ManufacturerService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/manufacturers`;
  }

  getAll(): Observable<ManufacturerDto[]> {
    return this.http.get<ManufacturerDto[]>(this.baseUrl);
  }

  create(dto: ManufacturerDto): Observable<ManufacturerDto> {
    return this.http.post<ManufacturerDto>(this.baseUrl, dto);
  }

  update(dto: ManufacturerDto): Observable<ManufacturerDto> {
    return this.http.put<ManufacturerDto>(this.baseUrl, dto);
  }

  delete(name: string, force = false): Observable<ManufacturerDto> {
    return this.http.delete<ManufacturerDto>(`${this.baseUrl}/${name}`, {params: {force}});
  }

  getDependents(name: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/${name}/dependents`);
  }
}
