import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UrlService} from '../../url/url-service';
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

  createComment(comment: string, worstCellId:number) {

    console.log("Create new comment",comment, worstCellId);
    const params = new HttpParams()
      .set('comment', comment)
      .set('worstCellId', worstCellId);

    return this.http.post<WorstCellCommentDto>(`${this.baseUrl}`, null, {params});
  }
}
