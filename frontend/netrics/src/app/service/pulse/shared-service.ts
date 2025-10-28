import {Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  selectedRat = signal<'ltefdd' | 'ltetdd' | 'nr' | 'umts' | 'gsm'>('ltefdd');
  selectedStandardKpi = signal('');
  selectedCell = signal('');
  district = signal('');
  granularity = signal<'day' | 'week' | 'month'>('day')
  excludeZeroes: boolean = false;
  currentPage: number = 0;
  //day.avg-busy.hr

}
