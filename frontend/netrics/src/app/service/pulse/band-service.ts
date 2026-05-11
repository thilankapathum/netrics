import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {BandDto} from '../../models/pulse/BandDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BandService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/bands`;
  }

  exportCellsWithMissingInfo() {
    return this.http.get(`${this.baseUrl}/missing/export`, {responseType: 'blob'});
  }

  getByRatName(ratName: string) {
    return this.http.get<Array<BandDto>>(`${this.baseUrl}/rat`, {params: {ratName}});
  }

  getAllBands(): Observable<BandDto[]> {
    return this.http.get<Array<BandDto>>(`${this.baseUrl}`);
  }

  getBandsByRatName(ratName: string) {
    return this.http.get<BandDto[]>(`${this.baseUrl}/rat`,{params: {ratName}});
  }
}
