import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {SectorDto} from '../../models/beam/SectorDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SectorService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/sectors`;
  }

  getBySiteCode(siteCode: string): Observable<SectorDto[]> {
    return this.http.get<SectorDto[]>(this.baseUrl, {params: {siteCode}});
  }

  createSector(sector: SectorDto): Observable<SectorDto> {
    return this.http.post<SectorDto>(this.baseUrl, sector);
  }

  updateSector(sector: SectorDto): Observable<SectorDto> {
    return this.http.put<SectorDto>(this.baseUrl, sector);
  }
}
