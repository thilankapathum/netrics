import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {StandardKpiDto} from '../../../models/pulse/StandardKpiDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StandardkpiService {

  private readonly baseUrl: string;
  // private readonly ratName:string = 'ltefdd';

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/standardkpi`;
  }

  getAllStandardKpi(ratName:string):Observable<StandardKpiDto[]> {
    // ra = this.ratName;
    return this.http.get<Array<StandardKpiDto>>(this.baseUrl,{ params: { ratName } });
  }

  createStandardKpi(dto: StandardKpiDto): Observable<StandardKpiDto> {
    return this.http.post<StandardKpiDto>(this.baseUrl, dto);
  }

  getAllStandardKpiWithOperands(ratName: string): Observable<StandardKpiDto[]> {
    return this.http.get<Array<StandardKpiDto>>(`${this.baseUrl}/operands`, { params: { ratName } });
  }

}
