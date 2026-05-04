import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BandDto} from '../../../models/pulse/BandDto';
import {MapCellDto} from '../../../models/pulse/MapCellDto';
import {Observable} from 'rxjs';
import {SiteDto} from '../../../models/pulse/SiteDto';

@Injectable({
  providedIn: 'root'
})
export class MapCellService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/map-cells`;
  }

  getBaseUrl(): string {
    return this.baseUrl;
  }

  getCellsByTile(z: number, x: number, y: number,
                 standardKpiName: string, ratName: string,
                 granularityName: string, date: string,
                 areaName: string): Observable<MapCellDto[]> {

    const params = new HttpParams()
      .set('standardKpiName', standardKpiName)
      .set('ratName', ratName)
      .set('granularityName', granularityName)
      .set('date', date)
      .set('areaName', areaName);

    return this.http.get<MapCellDto[]>(
      `${this.baseUrl}/tile/${z}/${x}/${y}`, {params}
    );
  }

  getCellsByTileAndBand(z: number, x: number, y: number,
                 standardKpiName: string, ratName: string,
                 granularityName: string, date: string,
                 areaName: string, bandName: string): Observable<MapCellDto[]> {

    const params = new HttpParams()
      .set('standardKpiName', standardKpiName)
      .set('ratName', ratName)
      .set('granularityName', granularityName)
      .set('date', date)
      .set('areaName', areaName)
      .set('bandName', bandName);

    return this.http.get<MapCellDto[]>(
      `${this.baseUrl}/tile-band/${z}/${x}/${y}`, {params}
    );
  }

  getSitesByTile(z: number, x: number, y: number):Observable<SiteDto[]> {
    return this.http.get<Array<SiteDto>>(`${this.baseUrl}/site-tile/${z}/${x}/${y}`)
  }

  getCellsByStandardKpi(minLng: number, minLat: number, maxLng: number, maxLat: number, standardKpiName: string, ratName: string, granularityName: string, date: string, areaName: string) {
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
