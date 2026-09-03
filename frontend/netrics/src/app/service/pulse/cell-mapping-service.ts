import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {Observable} from 'rxjs';
import {CellMappingDto} from '../../models/pulse/CellMappingDto';

@Injectable({
  providedIn: 'root'
})
export class CellMappingService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/cell-mappings`;
  }

  getAll(): Observable<CellMappingDto[]> {
    return this.http.get<CellMappingDto[]>(`${this.baseUrl}`);
  }

  create(dto: CellMappingDto): Observable<CellMappingDto> {
    return this.http.post<CellMappingDto>(`${this.baseUrl}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  importCellMappings(file: File): Observable<Blob> {
    const url = `${this.baseUrl}/import`;
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(url, formData, {responseType: 'blob'});
  }
}
