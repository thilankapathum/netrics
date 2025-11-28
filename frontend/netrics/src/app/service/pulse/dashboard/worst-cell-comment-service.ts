import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
import {WorstCell} from '../../../models/pulse/WorstCell';
import {WorstCellCommentDto} from '../../../models/pulse/WorstCellCommentDto';

@Injectable({
  providedIn: 'root'
})
export class WorstCellCommentService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/worst-cell-comments`;
  }

  getCommentByWorstCell(worstCellId:number) {
    return this.http.get<Array<WorstCellCommentDto>>(`${this.baseUrl}/cell/${worstCellId}`);
  }
}
