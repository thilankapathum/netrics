import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {InfraTypeDto} from '../../models/beam/InfraTypeDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InfraTypeService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/infra-types`;
  }

  getAll(): Observable<InfraTypeDto[]> {
    return this.http.get<InfraTypeDto[]>(this.baseUrl);
  }

  create(dto: InfraTypeDto): Observable<InfraTypeDto> {
    return this.http.post<InfraTypeDto>(this.baseUrl, dto);
  }

  update(dto: InfraTypeDto): Observable<InfraTypeDto> {
    return this.http.put<InfraTypeDto>(this.baseUrl, dto);
  }
}
