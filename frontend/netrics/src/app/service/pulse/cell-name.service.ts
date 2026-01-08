import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {CellNameDto} from '../../models/pulse/CellNameDto';

@Injectable({
  providedIn: 'root'
})
export class CellNameService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/cell-name`;
  }

  searchCell(name: string) {
    return this.http.get<Array<CellNameDto>>(`${this.baseUrl}`, {params: {name}});
  }

}
