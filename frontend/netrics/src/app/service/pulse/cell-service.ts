import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../url/url-service';
import {CellNameDto} from '../../models/pulse/CellNameDto';
import {Observable} from 'rxjs';
import {CellDto} from '../../models/pulse/CellDto';

@Injectable({
  providedIn: 'root'
})
export class CellService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/cells`;
  }

  exportCellsWithMissingInfo() {
    return this.http.get(`${this.baseUrl}/missing/export`, {responseType: 'blob'});
  }

  exportAllCells() {
    return this.http.get(`${this.baseUrl}/all/export`, {responseType: 'blob'});
  }

  importCellsWithCorrectedInfo(file: File): Observable<Blob> {
    const url = `${this.baseUrl}/missing/import`;
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(url, formData, {responseType: 'blob'});
  }

  getCellCountWithMissingInfo() {
    return this.http.get<number>(`${this.baseUrl}/missing/count`);
  }

  getCellByName(cellName: string): Observable<CellDto> {
    return this.http.get<CellDto>(`${this.baseUrl}`, {params: {cellName: cellName}});
  }

  getCellsBySector(sectorName:string, ratName:string):Observable<CellNameDto[]>{
    return this.http.get<CellNameDto[]>(`${this.baseUrl}/sector`, {params: {sectorName, ratName}});
  }

  updateCell(cell: CellDto):Observable<CellDto> {
    return this.http.put<CellDto>(`${this.baseUrl}`, cell);
  }
}
