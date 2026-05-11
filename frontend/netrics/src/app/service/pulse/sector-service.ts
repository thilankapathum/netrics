import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {Observable} from 'rxjs';
import {SectorDto} from '../../models/pulse/SectorDto';

@Injectable({
  providedIn: 'root'
})
export class SectorService {

  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/sectors`;
  }

  getSectorByName(name:string) {
    return this.http.get<SectorDto>(`${this.baseUrl}/${name}`);
  }

  searchSectorsByName(name:string) {
    return this.http.get<SectorDto[]>(`${this.baseUrl}/search`, {params:{name:name}});
  }

  exportSectorsWithMissingInfo() {
    return this.http.get(`${this.baseUrl}/missing/export`, {responseType: 'blob'});
  }

  exportAllSectors(){
    return this.http.get(`${this.baseUrl}/all/export`, {responseType: 'blob'});
  }

  importSectorsWithCorrectedInfo(file: File): Observable<Blob> {
    const url = `${this.baseUrl}/missing/import`;
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(url, formData, {responseType: 'blob'});
  }

  getSectorCountWithMissingInfo(){
    return this.http.get<number>(`${this.baseUrl}/missing/count`);
  }

}
