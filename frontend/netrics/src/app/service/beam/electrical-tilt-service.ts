import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {ElectricalTiltDto} from '../../models/beam/ElectricalTiltDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ElectricalTiltService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/electrical-tilts`;
  }

  getByAntennaId(antennaId: number): Observable<ElectricalTiltDto[]> {
    return this.http.get<ElectricalTiltDto[]>(this.baseUrl, {params: {antennaId}});
  }

  createElectricalTilt(tilt: ElectricalTiltDto): Observable<ElectricalTiltDto> {
    return this.http.post<ElectricalTiltDto>(this.baseUrl, tilt);
  }

  updateElectricalTilt(tilt: ElectricalTiltDto): Observable<ElectricalTiltDto> {
    return this.http.put<ElectricalTiltDto>(this.baseUrl, tilt);
  }

  deleteElectricalTilt(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
