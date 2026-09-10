import { Injectable } from '@angular/core';
import {OssDto} from '../../models/pulse/OssDto';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';

@Injectable({
  providedIn: 'root'
})
export class OssService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/oss`;
  }

  getAllOss(): Observable<OssDto[]> {
    return this.http.get<Array<OssDto>>(this.baseUrl);
  }

  createOss(dto: OssDto): Observable<OssDto> {
    return this.http.post<OssDto>(this.baseUrl, dto);
  }
}
