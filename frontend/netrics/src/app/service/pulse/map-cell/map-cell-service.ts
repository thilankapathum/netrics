import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BandDto} from '../../../models/pulse/BandDto';
import {MapCellDto} from '../../../models/pulse/MapCellDto';

@Injectable({
  providedIn: 'root'
})
export class MapCellService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/map-cells`;
  }

  getCellsByStandardKpi(minLng: number, minLat: number, maxLng: number, maxLat: number, standardKpiName: string, ratName: string, granularityName: string, date:string, areaName:string) {
    return this.http.get<Array<MapCellDto>>(`${this.baseUrl}/standard-kpi`, {
      params: {
        minLng,
        minLat,
        maxLng,
        maxLat,
        standardKpiName,
        ratName,
        granularityName,
        date,
        areaName
      }
    });
  }
}
