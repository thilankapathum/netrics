import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UrlService} from '../url/url-service';
import {AssistantRequestDto, AssistantResponseDto} from '../../models/pulse/AssistantDto';

@Injectable({
  providedIn: 'root'
})
export class AssistantService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient, private urlService: UrlService) {
    this.baseUrl = `${this.urlService.getPulseUrl()}/assistant`;
  }

  ask(dto: AssistantRequestDto): Observable<AssistantResponseDto> {
    return this.http.post<AssistantResponseDto>(`${this.baseUrl}/ask`, dto);
  }
}
