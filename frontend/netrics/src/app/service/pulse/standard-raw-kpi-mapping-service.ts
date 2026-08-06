import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {Observable} from 'rxjs';
import {StandardRawKpiMappingDto} from '../../models/pulse/StandardRawKpiMappingDto';
import {StandardKpiDto} from '../../models/pulse/StandardKpiDto';

@Injectable({
  providedIn: 'root'
})
export class StandardRawKpiMappingService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/standard-raw-kpimap`;
  }

  getAll(ratName: string): Observable<StandardRawKpiMappingDto[]> {
    return this.http.get<Array<StandardRawKpiMappingDto>>(this.baseUrl, { params: { ratName } });
  }

  createMapping(dto: StandardRawKpiMappingDto): Observable<StandardRawKpiMappingDto> {
    return this.http.post<StandardRawKpiMappingDto>(this.baseUrl, dto);
  }

  isMappingAvailable(ratName: string, standardKpiName: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/is-available`, {
      params: {
        ratName: ratName,
        standardKpiName: standardKpiName
      }
    });
  }

}
