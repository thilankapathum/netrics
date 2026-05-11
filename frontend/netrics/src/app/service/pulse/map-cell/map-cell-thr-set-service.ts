import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {MapCellThrSetResponseDto} from '../../../models/pulse/map-cell/MapCellThrSetResponseDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MapCellThrSetService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/map-cell-thr-sets`;
  }

  createThrSet(thrSet: MapCellThrSetResponseDto): Observable<MapCellThrSetResponseDto> {
    return this.http.post<MapCellThrSetResponseDto>(`${this.baseUrl}`, thrSet)
  }

  getThrSetResponse(standardKpiName: string, ratName: string, granularityName: string, isAdmin: boolean) {
    const params = new HttpParams()
      .set('standardKpiName', standardKpiName)
      .set('ratName', ratName)
      .set('granularityName', granularityName)
      .set('isAdmin', isAdmin);

    return this.http.get<MapCellThrSetResponseDto>(`${this.baseUrl}`,{params})
  }

}
