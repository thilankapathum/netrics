import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {BeamBandDto} from '../../models/beam/BeamBandDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BandService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getBeamUrl()}/bands`;
  }

  getAll(): Observable<BeamBandDto[]> {
    return this.http.get<BeamBandDto[]>(this.baseUrl);
  }
}
