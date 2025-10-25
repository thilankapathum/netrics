import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {RatDto} from '../../models/pulse/RatDto';
import {CellNameDto} from '../../models/pulse/CellNameDto';
import {Observable} from 'rxjs';
import {KpiDataDto} from '../../models/pulse/KpiDataDto';

@Injectable({
  providedIn: 'root'
})
export class CellService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/cell`;
  }

  searchCell(name: string) {
    return this.http.get<Array<CellNameDto>>(`${this.baseUrl}`, {params: {name}});
  }

}
