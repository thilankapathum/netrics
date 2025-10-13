import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {BasicKpiDto} from '../../../models/pulse/BasicKpiDto';

@Injectable({
  providedIn: 'root'
})
export class LtefddbasickpiService {

  private readonly baseUrl: string;
  private readonly ratName: string = 'ltefdd';

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/basickpi`;
  }

  getAllBasicKpi() {
    const ratName: string = this.ratName;
    return this.http.get<Array<BasicKpiDto>>(this.baseUrl, {params: {ratName}});
  }
}
