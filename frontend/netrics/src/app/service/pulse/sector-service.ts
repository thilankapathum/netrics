import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {Observable} from 'rxjs';
import {SectorDto} from '../../models/pulse/SectorDto';

@Injectable({
  providedIn: 'root'
})
export class SectorService {

  private readonly getUrl: string;
  private readonly postUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.getUrl = `${this.urlService.getPulseUrl()}/sectors`;
    this.postUrl = `${this.urlService.getBeamUrl()}/sectors`;
  }

  getSectorByName(name:string) {
    return this.http.get<SectorDto>(`${this.getUrl}/${name}`);
  }

  searchSectorsByName(name:string) {
    return this.http.get<SectorDto[]>(`${this.getUrl}/search`, {params:{name:name}});
  }

  exportSectorsWithMissingInfo() {
    return this.http.get(`${this.postUrl}/missing/export`, {responseType: 'blob'});
  }

  exportAllSectors(){
    return this.http.get(`${this.postUrl}/all/export`, {responseType: 'blob'});
  }

  importSectorsWithCorrectedInfo(file: File): Observable<Blob> {
    const url = `${this.postUrl}/missing/import`;
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(url, formData, {responseType: 'blob'});
  }

  getSectorCountWithMissingInfo(){
    return this.http.get<number>(`${this.postUrl}/missing/count`);
  }

}
