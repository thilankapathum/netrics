import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {MapCellThresholdDto} from '../../../models/pulse/map-cell/MapCellThresholdDto';
import {Observable} from 'rxjs';
import {List} from 'postcss/lib/list';
import {MapCellThrSetAndThresholds} from '../../../models/pulse/map-cell/MapCellThrSetAndThresholds';

@Injectable({
  providedIn: 'root'
})
export class MapCellThresholdService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/map-cell-thresholds`;
  }

  createThreshold(threshold: MapCellThresholdDto): Observable<MapCellThresholdDto> {
    return this.http.post<MapCellThresholdDto>(`${this.baseUrl}`, threshold);
  }

  getThresholdsByThrSetId(id: number): Observable<Array<MapCellThresholdDto>> {
    return this.http.get<Array<MapCellThresholdDto>>(`${this.baseUrl}/thr-set`, {params: {id}});
  }

  getThrSetAndThresholdsByThrSetId(id: number): Observable<MapCellThrSetAndThresholds> {
    return this.http.get<MapCellThrSetAndThresholds>(`${this.baseUrl}/thr-set-thresholds/${id}`);
  }

  getThrSetAndThresholds(standardKpiName: string, ratName: string, granularityName: string, isAdmin: boolean): Observable<MapCellThrSetAndThresholds> {
    const params = new HttpParams()
      .set('standardKpiName', standardKpiName)
      .set('ratName', ratName)
      .set('granularityName', granularityName)
      .set('isAdmin', isAdmin);

    return this.http.get<MapCellThrSetAndThresholds>(`${this.baseUrl}/thr-set-thresholds`, {params});
  }
}
