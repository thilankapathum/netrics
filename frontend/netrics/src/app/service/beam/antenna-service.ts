import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {AntennaDto} from '../../models/beam/AntennaDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AntennaService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/antennas`;
  }

  getBySectorName(sectorName: string): Observable<AntennaDto[]> {
    return this.http.get<AntennaDto[]>(this.baseUrl, {params: {sectorName}});
  }

  createAntenna(antenna: AntennaDto): Observable<AntennaDto> {
    return this.http.post<AntennaDto>(this.baseUrl, antenna);
  }

  updateAntenna(antenna: AntennaDto): Observable<AntennaDto> {
    return this.http.put<AntennaDto>(this.baseUrl, antenna);
  }

  deleteAntenna(antenna: AntennaDto): Observable<AntennaDto> {
    return this.http.request<AntennaDto>('delete', this.baseUrl, {body: antenna});
  }
}
